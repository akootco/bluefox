package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.util.IOUtil
import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import java.io.File
import kotlin.io.path.Path
import kotlin.reflect.KProperty

class BlueFox : FoxPlugin() {

    companion object {

        lateinit var settings: FoxConfig
        lateinit var auth: FoxConfig

        fun getAPIKey(name: String): String? {
            return auth.getString("tokens.$name")
        }

        fun getToken(name: String): String? {
            return auth.getString("tokens.$name")
        }

        val test get() = settings.getString("test")
    }

    override fun register() {
        logger.info("Good day!")
    }

    override fun unregister() {
        // Plugin shutdown logic
    }

    override fun registerConfigs() {
        settings = registerConfig("settings")
        auth = registerConfig("auth")
    }

}
