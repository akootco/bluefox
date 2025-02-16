package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.util.DbConfig
import co.akoot.plugins.bluefox.util.IOUtil
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import java.sql.Connection

class BlueFox : FoxPlugin("bluefox") {

    companion object {

        lateinit var settings: FoxConfig
        lateinit var auth: FoxConfig
        var profiles: DbConfig? = null
        lateinit var server: Server
        lateinit var overworld: World
        lateinit var spawnLocation: Location
        lateinit var instance: BlueFox

        var cachedOfflinePlayerNames = mutableSetOf<String>()

        fun getAPIKey(name: String): String? {
            if (!this::auth.isInitialized) return null
            return auth.getString("api-keys.$name")
        }

        fun getToken(name: String): String? {
            if (!this::auth.isInitialized) return null
            return auth.getString("tokens.$name")
        }

        fun getPlayer(name: String, exact: Boolean = false): Player? {
            if (exact) return server.onlinePlayers.find { it.name.equals(name, true) }
            return server.onlinePlayers.find { it.name.startsWith(name, true) }
        }

        fun getOfflinePlayer(name: String, exact: Boolean = false): OfflinePlayer? {
            if (exact) return cachedOfflinePlayerNames.find { it.equals(name, true) }?.let { server.getOfflinePlayer(it) }
            return cachedOfflinePlayerNames.find {it.startsWith(name, true)}?.let { server.getOfflinePlayer(it) }
        }

    }

    private lateinit var mainDataSource: HikariDataSource
    private fun setupDatabases() {
        try {
            val hikariConfig = HikariConfig().apply {
                jdbcUrl = "jdbc:mysql://${auth.getString("mysql.url")}:3306/${auth.getString("mysql.database")}"
                username = auth.getString("mysql.username")
                password = auth.getString("mysql.password")
                driverClassName = "com.mysql.cj.jdbc.Driver"
                maximumPoolSize = 10
                minimumIdle = 2
                idleTimeout = 10000
                connectionTimeout = 30000
            }
            mainDataSource = HikariDataSource(hikariConfig)
            val mainDb = mainDataSource.connection
            profiles = DbConfig(mainDb, "profiles", "uuid") //TODO fix this
            //logger.info("Database - Loaded $userCount users.")
        } catch (_: Exception) {
            logger.severe("Failed to connect to the database, related features disabled.")
        }
    }

    fun getJarFilesInPluginsFolder(): List<String> {
        val candidates = mutableListOf<String>()
        val files = server.pluginsFolder.listFiles() ?: return listOf()
        for(file in files) {
            if(file.isDirectory || !file.name.endsWith(".jar")) continue
            candidates += file.name
        }
        return candidates
    }

    /**
     * Load a plugin
     *
     * @param pluginName The name of the plugin
     * @return Whether the plugin was loaded
     */
    fun loadPlugin(pluginName: String): Boolean {
        val pluginFile = server.pluginsFolder.resolve("$pluginName.jar")
        if (name !in getJarFilesInPluginsFolder()) return false
        if (!IOUtil.jarContainsFile(pluginFile.absolutePath, "plugin.yml")) return false
        runCatching {
            server.pluginManager.loadPlugin(pluginFile)
            return true
        }
        return false
    }

    /**
     *
     * Unload a plugin, if it is loaded
     *
     * @param pluginName The name of the plugin
     * @return Whether the plugin was unloaded
     *
     * Turns out loading plugins is easy, unloading them takes some work!
     *
     * As per instructions:
     *     Remove the plugin from the list of plugins on the server.
     *     Remove the plugin name from the list of plugin names on the server.
     *     Remove the plugin commands from the command map and known commands.
     *     Unregister all listeners.
     *     Set specific fields to null in the plugin classloader. (At least plugin and pluginInit. If you want to be on the safe side, iterate over all of them.)
     *     Close the class loader. // Optional, but a safe step to take.
     *     Collect garbage.
     *
     */
    fun unloadPlugin(pluginName: String): Boolean {

        val pluginManager = server.pluginManager
        val plugin = pluginManager.getPlugin(pluginName) ?: return false

        // Disable the plugin
        pluginManager.disablePlugin(plugin)

        runCatching {
            // Get the plugin manager and field for the plugins map
            val pluginManagerClass = pluginManager.javaClass
            val pluginsField = pluginManagerClass.getDeclaredField("plugins")
            val lookupNamesField = pluginManagerClass.getDeclaredField("lookupNames")
            val commandMapField = pluginManagerClass.getDeclaredField("commandMap")
            val listenersField = pluginManagerClass.getDeclaredField("listeners")

            pluginsField.isAccessible = true
            lookupNamesField.isAccessible = true
            commandMapField.isAccessible = true
            listenersField.isAccessible = true

            val plugins = pluginsField.get(pluginManager) as MutableList<*>
            val lookupNames = lookupNamesField.get(pluginManager) as MutableMap<*, *>
            val commandMap = commandMapField.get(pluginManager)
            val listeners = listenersField.get(pluginManager) as MutableMap<*, *>

            // Remove from plugins and lookupNames
            plugins.remove(plugin)
            lookupNames.remove(plugin.name)

            // Remove commands
            val knownCommandsField = commandMap.javaClass.getDeclaredField("knownCommands")
            knownCommandsField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val knownCommands = knownCommandsField.get(commandMap) as MutableMap<String, *>
            knownCommands.entries.removeIf { it.value?.javaClass?.name?.startsWith(plugin.javaClass.name) == true }

            // Unregister event listeners
            listeners.entries.removeIf { it.key?.javaClass?.name?.startsWith(plugin.javaClass.name) == true }

            // Unload class loader
            val pluginClassLoader = plugin.javaClass.classLoader
            if (pluginClassLoader is java.net.URLClassLoader) {
                pluginClassLoader.close()
            }

            System.gc() // Garbage Collection
            return true
        }

        return false
    }

    override fun onEnable() {
        super.onEnable()
        instance = this
    }

    override fun load() {
        BlueFox.server = server
        overworld = server.getWorld(settings.getString("overworld") ?: "world") ?: return
        spawnLocation = settings.getLocation("spawnLocation") ?: overworld.spawnLocation
        setupDatabases()
        cachedOfflinePlayerNames = server.offlinePlayers.mapNotNull { it.name }.toMutableSet()
        logger.info("Good day!")
    }

    override fun unload() {
        if (!this::mainDataSource.isInitialized) return
        mainDataSource.close()
    }

    override fun registerConfigs() {
        settings = registerConfig("settings")
        auth = registerConfig("auth")
    }

    override fun onCrash() {
        server.shutdown()
    }

}
