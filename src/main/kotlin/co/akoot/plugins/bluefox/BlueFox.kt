package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.api.LegacyWarp
import co.akoot.plugins.bluefox.api.economy.Market
import co.akoot.plugins.bluefox.commands.DelHomeCommand
import co.akoot.plugins.bluefox.commands.HomeCommand
import co.akoot.plugins.bluefox.commands.HomesCommand
import co.akoot.plugins.bluefox.commands.MarketCommand
import co.akoot.plugins.bluefox.commands.SetHomeCommand
import co.akoot.plugins.bluefox.commands.TradeCommand
import co.akoot.plugins.bluefox.commands.UserHomeCommand
import co.akoot.plugins.bluefox.commands.UserHomesCommand
import co.akoot.plugins.bluefox.commands.WalletCommand
import co.akoot.plugins.bluefox.extensions.legacyName
import co.akoot.plugins.bluefox.listeners.BlueFoxListener
import co.akoot.plugins.bluefox.util.IOUtil
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.coreprotect.CoreProtect
import net.coreprotect.CoreProtectAPI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.geysermc.api.Geyser
import org.geysermc.api.GeyserApiBase
import java.io.File
import java.sql.Connection

class BlueFox : FoxPlugin("bluefox") {

    companion object {
        lateinit var auth: FoxConfig
        lateinit var server: Server
        lateinit var spawnLocation: Location
        lateinit var instance: BlueFox
        lateinit var db: Connection

        var world: World? = null

        val geyser: GeyserApiBase? get() = instance.getGeyser()
        val co: CoreProtectAPI? get() = instance.getCoreProtect()


        var cachedOfflinePlayerNames = mutableSetOf<String>()

        fun getPlayer(name: String, exact: Boolean = false): Player? {
            if (exact) return server.onlinePlayers.find { it.name.equals(name, true) }
            return server.onlinePlayers.find { it.name.startsWith(name, true) }
        }

        fun getPlayers(playerNames: String, exact: Boolean = false): List<Player> {
            val playerNames = playerNames.split(",", ", ", ", and ", " and ", " ", "/", " / ", "&", " & ", ignoreCase = true)
            return playerNames.mapNotNull { getPlayer(it, exact) }
        }

        fun getOfflinePlayer(name: String, exact: Boolean = false): OfflinePlayer? {
            if (exact) return cachedOfflinePlayerNames.find { it.equals(name, true) }?.let { server.getOfflinePlayer(it) }
            return cachedOfflinePlayerNames.find {it.startsWith(name, true)}?.let { server.getOfflinePlayer(it) }
        }

        fun key(key: String): NamespacedKey {
            return NamespacedKey(instance, key)
        }

        fun getLegacyWorld(legacyName: String): World? {
            val name = when(legacyName) {
                "nether" -> "world_nether"
                "end" -> "world_the_end"
                else -> legacyName
            }
            return Bukkit.getWorld(name)
        }

    }

    val legacyWarps: MutableSet<LegacyWarp> = mutableSetOf()

    fun getCoreProtect(): CoreProtectAPI? {
        val plugin = server.pluginManager.getPlugin("CoreProtect")

        // Check that CoreProtect is loaded
        if (plugin == null || plugin !is CoreProtect) {
            return null
        }

        // Check that the API is enabled
        val coreProtect = plugin.api
        if (!coreProtect.isEnabled) {
            return null
        }

        // Check that a compatible version of the API is loaded
        if (coreProtect.APIVersion() < 10) {
            return null
        }

        return coreProtect
    }

    private fun getGeyser(): GeyserApiBase? {
        val plugin = server.pluginManager.getPlugin("Geyser-Spigot") ?: return null
        return Geyser.api()
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
            db = mainDataSource.connection
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

    override fun load() {
        instance = this
        BlueFox.server = server
        world = server.getWorld("world")
        setupDatabases()
        Market.load()
        cachedOfflinePlayerNames = server.offlinePlayers.mapNotNull { it.name }.toMutableSet()
        loadLegacyWarps()
        logger.info("Good day!")
    }

    private val legacyWarpsFolder = File("warps")
    fun loadLegacyWarps() {
        for (file in legacyWarpsFolder.listFiles {
            it.isFile && it.name.endsWith(".json")
        }) {
            val warp = getLegacyWarp(file.name.substringBeforeLast('.')) ?: continue
            legacyWarps += warp
        }
    }

    fun getLegacyWarp(name: String): LegacyWarp? {
        val file = legacyWarpsFolder.resolve("$name.json")
        if(!file.exists()) return null
        val config = FoxConfig(file)
        val name = config.getString("name") ?: return null
        val world = config.getString("world")?.let { getLegacyWorld(it) } ?: return null
        val coordinates = config.getDoubleList("coordinates").ifEmpty { return null }
        val direction = config.getDoubleList("direction").ifEmpty { return null }
        val location = Location(world, coordinates[0], coordinates[1], coordinates[2], direction[0].toFloat(), direction[1].toFloat())
        return LegacyWarp(name, location)
    }

    /**
     * @return true if replaced
     */
    fun setLegacyWarp(warp: LegacyWarp): Boolean {
        val file = legacyWarpsFolder.resolve("$name.json")
        val config = FoxConfig(file)
        config.autosave = false
        config.set("name", warp.name)
        config.set("world", getLegacyWorld(warp.location.world.legacyName))
        config.set("coordinates", listOf(warp.location.x, warp.location.y, warp.location.z))
        config.set("direction", listOf(warp.location.yaw, warp.location.pitch))
        config.save()
        return file.exists()
    }

    /**
     * @return true if deleted
     */
    fun deleteLegacyWarp(warpName: String): Boolean {
        val file = legacyWarpsFolder.resolve("${warpName}.json")
        return file.delete()
    }

    override fun unload() {
        if (!this::mainDataSource.isInitialized) return
        mainDataSource.close()
    }

    override fun registerConfigs() {
        auth = registerConfig("auth")
    }

    override fun registerCommands() {
        registerCommand(WalletCommand(this))
        registerCommand(TradeCommand(this))
        registerCommand(MarketCommand(this))
        registerCommand(HomeCommand(this))
        registerCommand(HomesCommand(this))
        registerCommand(SetHomeCommand(this))
        registerCommand(DelHomeCommand(this))
        registerCommand(UserHomeCommand(this))
        registerCommand(UserHomesCommand(this))
//        registerCommand(TestCommand(this))
    }

    override fun onCrash() {
        server.shutdown()
    }

    override fun registerEvents() {
        registerEventListener(BlueFoxListener())
    }

}
