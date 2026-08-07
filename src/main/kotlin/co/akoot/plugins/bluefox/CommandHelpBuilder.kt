package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.extensions.mix
import co.akoot.plugins.bluefox.util.Color
import co.akoot.plugins.bluefox.util.colorCodes
import co.akoot.plugins.bluefox.util.hover
import co.akoot.plugins.bluefox.util.italic
import co.akoot.plugins.bluefox.util.parse
import co.akoot.plugins.bluefox.util.plus
import co.akoot.plugins.bluefox.util.primary
import co.akoot.plugins.bluefox.util.quote
import co.akoot.plugins.bluefox.util.suggest
import co.akoot.plugins.bluefox.util.text
import co.akoot.plugins.bluefox.util.zip
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

fun commandHelp(block: CommandHelpBuilder.() -> Unit): Component {
    return CommandHelpBuilder()
        .apply(block)
        .build()
}

class CommandHelpBuilder {
    private val text: MutableList<Component> = mutableListOf()

    private val colors = listOf(
        colorCodes["7"],
        colorCodes["b"],
        colorCodes["e"],
        colorCodes["a"],
        colorCodes["d"],
        colorCodes["6"]
    )

    private var colorIndex = 0
    fun nextColor(): TextColor {
        if(colorIndex >= colors.size) colorIndex = 0
        return colors[colorIndex++] ?: Color.White
    }

    fun color(commandLine: String): Component {
        val builder = Component.text("  ").toBuilder()
        val parts = commandLine.split(" ")
        for((i, part) in parts.withIndex()) {
            builder.append(colors[i % colors.size] + part.replace("\\", " "))
            builder.append(Component.space())
        }
        return builder.build().clickEvent(suggest(commandLine.replace("\\", " "))).italic()
    }

    fun resetColor() { colorIndex = 0 }

    fun title(string: String): CommandHelpBuilder {
        resetColor()
        text += quote("\n -= [ ")
        text += Component.text(string)
        text += quote(" ] =-\n\n")
        return this
    }

    fun message(string: String): CommandHelpBuilder {
        text += string.parse()
        return this
    }

    fun bullet(string: String = "", quote: Boolean = true, color: Boolean = true): CommandHelpBuilder {
        val finalColor = if(color && quote) nextColor().mix(Color.Quote)
        else if (!color && quote) Color.Quote
        else if (!color) null
        else nextColor()
        text += "• $string\n".parse().color(finalColor)
        return this
    }

    fun description(description: String): CommandHelpBuilder {
        text += quote(description)
        return this
    }

    fun newline(lines: Int = 1): CommandHelpBuilder {
        text += Component.text("\n".repeat(lines))
        return this
    }

    fun usage(
        part: String,
        hover: String? = null,
        optional: Boolean = false,
        literal: Boolean = false,
        list: Boolean = false,
        last: Boolean = false,
    ): CommandHelpBuilder {
        val first = part.startsWith("/")
        val word = when {
            literal || first -> part
            optional -> "[$part]"
            list -> "[$part...]"
            else -> "<$part>"
        }
        if(first) resetColor()
        val color = nextColor()
        var component: Component = Component.text(word, color)
        if(hover != null) component = component.hover(color + hover)
        if(first) text += Component.text("• ").color(color)
        text += component
        text += if (last) Component.newline()
        else Component.space()
        return this
    }

    fun example(commandLine: String, description: String? = null, last: Boolean = false): CommandHelpBuilder {
        val color = nextColor().mix(Color.White, 0.75)
        if (description != null) text += Component.text("• $description\n", color)
        text += color(commandLine)
        newline(if(last)1 else 2)
        return this
    }

    fun build(): Component = text.zip
}