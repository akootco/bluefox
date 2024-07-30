package co.akoot.plugins.bluefox.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import java.awt.Color

class Txt(val string: String, val color: TextColor) {

    constructor(string: String) : this(string, ColorUtil.WHITE)
    constructor(string: String, color: Int) : this(string, TextColor.color(color))
    constructor(string: String, color: Color) : this(string, TextColor.color(color.rgb))
    constructor(string: String, color: String, bedrock: Boolean = false): this(string, ColorUtil.getColor(color, bedrock))

    private val builder = Component.text(string).color(color).toBuilder()

    fun suggest(command: String): Txt {
        builder.clickEvent(ClickEvent.suggestCommand(command))
        return this
    }

    fun run(command: String): Txt {
        builder.clickEvent(ClickEvent.runCommand(command))
        return this
    }

    fun hover(text: Txt): Txt {
        builder.hoverEvent(HoverEvent.showText(text.c))
        return this
    }

    fun hover(text: String): Txt {
        builder.hoverEvent(HoverEvent.showText(Component.text(text)))
        return this
    }

    fun url(url: String): Txt {
        builder.clickEvent(ClickEvent.openUrl(url))
        return this
    }

    val c: Component get() = builder.build()
}