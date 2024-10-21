package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.util.DbConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Material
import java.sql.Connection

class BlueFox : FoxPlugin() {

    companion object {

        lateinit var settings: FoxConfig
        lateinit var auth: FoxConfig
        lateinit var usersDb: Connection

        fun getAPIKey(name: String): String? {
            if (!this::auth.isInitialized) return null
            return auth.getString("api-keys.$name")
        }

        fun getToken(name: String): String? {
            if (!this::auth.isInitialized) return null
            return auth.getString("tokens.$name")
        }

        val enumTest get() = settings.getEnum(Material::class.java, "enumTest")

    }

    private lateinit var usersDataSource: HikariDataSource
    private fun setupDatabases() {
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
    }

    override fun load() {
        setupDatabases()
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

}
