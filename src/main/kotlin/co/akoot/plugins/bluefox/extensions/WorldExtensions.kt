package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.util.Color
import co.akoot.plugins.bluefox.util.Text
import io.papermc.paper.math.BlockPosition
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.World
import org.bukkit.block.Block

val World.legacyName: String
    get() {
        return when (name) {
            "world_nether" -> "nether"
            "world_the_ender" -> "end"
            else -> name
        }
    }

val World.executeName: String
    get() {
        return when (name) {
            "world_the_end" -> "minecraft:the_end"
            "world_nether" -> "minecraft:the_nether"
            "world" -> "minecraft:overworld"
            else -> name
        }
    }

fun World.text(color: TextColor = Kolor.TEXT.accent): Text {
    val envColor = when (environment) {
        World.Environment.NETHER -> TextColor.color(0xff0000)
        World.Environment.THE_END -> TextColor.color(0xff00ff)
        World.Environment.NORMAL -> TextColor.color(0x00ff00)
        else -> TextColor.color(0x000000)
    }
    return Text(name, color.mix(envColor))
}

fun World.component(color: TextColor = Color.Primary): Component {
    val envColor = when (environment) {
        World.Environment.NETHER -> TextColor.color(0xff0000)
        World.Environment.THE_END -> TextColor.color(0xff00ff)
        World.Environment.NORMAL -> TextColor.color(0x00ff00)
        else -> TextColor.color(0x000000)
    }
    return Component.text(name, color.mix(envColor))
}

fun World.blockAt(blockPos: BlockPosition): Block {
    return getBlockAt(blockPos.blockX(), blockPos.blockY(), blockPos.blockZ())
}