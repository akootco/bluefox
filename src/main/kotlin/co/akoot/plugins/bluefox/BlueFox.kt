package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.util.IOUtil
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File
import kotlin.io.path.Path
import kotlin.reflect.KProperty

class BlueFox : FoxPlugin() {

    companion object {

        lateinit var settings: Config
        lateinit var auth: Config

        fun getAPIKey(name: String): String? {
            return try {
                auth.getString("api-keys.$name")
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun getToken(name: String): String? {
            return try {
                auth.getString("tokens.$name")
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        val test get() = settings.getString("test")
    }

    override fun register() {
        checkConfigs()
        logger.info("Good day!")
    }

    override fun unregister() {
        // Plugin shutdown logic
    }

    private fun checkConfigs() {
        if(dataFolder.mkdirs()) logger.info("Created data folder")

        val settingsFile = File(dataFolder, "settings.conf")
        val authFile = File(dataFolder, "auth.conf")

        val configFiles = setOf(settingsFile, authFile)

        val classLoader = FoxPlugin::class.java.classLoader
        for (file in configFiles) {
            if (file.exists()) continue
            if(IOUtil.extractFile(classLoader, file.name, file.toPath())) {
                logger.info("Loaded config ${file.name} from jar")
            }
        }

        settings = ConfigFactory.parseFile(settingsFile)
        auth = ConfigFactory.parseFile(authFile)
    }
}
