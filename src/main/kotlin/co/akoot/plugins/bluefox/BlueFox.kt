package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.util.DbConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import java.sql.Connection

class BlueFox : FoxPlugin("bluefox") {

    companion object {

        lateinit var settings: FoxConfig
        lateinit var auth: FoxConfig
        lateinit var usersDb: Connection
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

    private lateinit var usersDataSource: HikariDataSource
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
            usersDataSource = HikariDataSource(hikariConfig)
            usersDb = usersDataSource.connection
        } catch (ex: Exception) {
            ex.printStackTrace()
            crash()
        }
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
        if (!this::usersDataSource.isInitialized) return
        usersDataSource.close()
    }

    override fun registerConfigs() {
        settings = registerConfig("settings")
        auth = registerConfig("auth")
    }

    override fun onCrash() {
        server.shutdown()
    }

}
