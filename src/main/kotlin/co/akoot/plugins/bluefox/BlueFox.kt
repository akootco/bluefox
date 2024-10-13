package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.util.IOUtil
import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import org.bukkit.Material
import java.io.File
import kotlin.io.path.Path
import kotlin.reflect.KProperty

class BlueFox : FoxPlugin() {

    companion object {

        lateinit var settings: FoxConfig
        lateinit var auth: FoxConfig

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

    override fun load() {
        logger.info("Good day!")
    }

    override fun unload() {
        // Plugin shutdown logic
    }

    override fun registerConfigs() {
        settings = registerConfig("settings")
        auth = registerConfig("auth")
    }

}
