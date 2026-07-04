package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.util.parse
import net.kyori.adventure.text.Component

object Titles {
    private val titles = FoxConfig(BlueFox.instance.dataFolder.resolve("titles.conf"))
    val list = titles.conf.root().keys
    fun get(name: String): Component = (titles.getString(name) ?: name).parse()
}