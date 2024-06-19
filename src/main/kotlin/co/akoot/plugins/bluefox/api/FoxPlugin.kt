package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.util.ColorUtil
import org.bukkit.command.Command
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Color

abstract class FoxPlugin: JavaPlugin() {

    val bf get() = if(this is BlueFox) this else BlueFox.instance

    open val color = mapOf(
        "text" to Color(0xfcf7f4),
        "accent" to Color(0x97d4fc),
        "player" to Color(0xfc97c9),
        "number" to Color(0xfce497),
        "error_text" to Color(0xfc5f5f),
        "error_accent" to Color(0xfc8888),
        "error_player" to Color(0xfc88c2),
        "error_number" to Color(0xfcae5f),
    )

    val colorTinted = color.mapValues { (_, color) ->
        ColorUtil.mix(color, bf.monthColor)
    }

    open val colorBedrock = mapOf(
        "text" to Color(0xFFFFFF),
        "accent" to Color(0x55FFFF),
        "player" to Color(0xFF55FF),
        "number" to Color(0xFFFF55),
        "error_text" to Color(0xAA0000),
        "error_accent" to Color(0xFF5555),
        "error_player" to Color(0xFF5555),
        "error_number" to Color(0xFF5555),
    )

    fun getColor(name: String, bedrock: Boolean = false): Color {
        return (if(bedrock) colorBedrock[name] else color[name]) ?: Color.WHITE
    }

    fun getErrorColor(name: String, bedrock: Boolean = false): Color {
        return (if(bedrock) colorBedrock["error_$name"] else color["error_$name"]) ?: Color.WHITE
    }

    override fun onEnable() {
        // TODO: stuff
        register()
    }
    override fun onDisable() {
        // TODO: Stuff
        unregister()
    }

    abstract fun register()
    abstract fun unregister()
    open fun registerCommands() {}
    open fun registerEvents() {}

    fun registerCommand(command: FoxCommand) {
        server.commandMap.register(command.name, command)
    }

    fun registerEventListener(listener: Listener) {
        server.pluginManager.registerEvents(listener, this)
    }
}