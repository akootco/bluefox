package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.CatCommand
import co.akoot.plugins.bluefox.extensions.hex
import co.akoot.plugins.bluefox.util.copy
import co.akoot.plugins.bluefox.util.hover
import co.akoot.plugins.bluefox.util.join
import co.akoot.plugins.bluefox.util.palettes
import co.akoot.plugins.bluefox.util.plus
import co.akoot.plugins.bluefox.util.primary
import co.akoot.plugins.bluefox.util.sendMessage
import co.akoot.plugins.bluefox.util.sendWarning
import co.akoot.plugins.bluefox.util.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.CommandSender

class PalettesCommand(plugin: BlueFox): CatCommand(plugin, "palettes") {
    init {
        noargs {
            permissionCheck(it) ?: return@noargs false
            val sender = getSender(it)
            palettes.entries.sortedBy { e -> e.value.size }
                .associate { e -> e.toPair() }.forEach { (palette, colors) ->
                sendPalette(sender, palette, colors)
                    sender.sendMessage("")
            }
            true
        }
    }
}

class PaletteCommand(plugin: BlueFox): CatCommand(plugin, "palette") {
    init {
        then {
            string("palette", suggestions = { _, builder -> suggest(builder, palettes.keys.toList()) }) {
                permissionCheck(it) ?: return@string false
                val sender = getSender(it)
                val palette = getString(it, "palette")
                val colors = palettes[palette] ?: return@string sender.sendWarning("Palette not found")
                sendPalette(sender, palette, colors)
                true
            }
        }
    }
}

fun sendPalette(sender: CommandSender, name: String, colors: List<TextColor>) {
    val e = colors.mapIndexed { i, color ->
        (color + "█").clickEvent(copy("&$name$i")).hover(color.hex())
    }.toMutableList().join("")
    sender.sendMessage(e.append(text(" - ", primary(name)).join("").clickEvent(copy("&$name>"))).hover("&$name>"))
}