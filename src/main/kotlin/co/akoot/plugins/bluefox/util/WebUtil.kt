package co.akoot.plugins.bluefox.util

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigSyntax
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

object WebUtil {

    fun getUrl(url: String): URL? {
        return try {
            URI(url).toURL()
        } catch (_: Exception) {
            null
        }
    }

    fun getConfig(url: URL): Config? {
        return try {
            ConfigFactory.parseURL(url, ConfigParseOptions.defaults().setSyntax(ConfigSyntax.JSON))
        } catch (e: Exception){
            e.printStackTrace()
            null
        }
    }


    fun getJsonString(url: String): String? {
        return getUrl(url)?.let { getJsonString(it) }
    }

    fun getJsonString(url: URL): String? {
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            return null
        }
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()
        return response
    }
}