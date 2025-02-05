package co.akoot.plugins.bluefox.util

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.User
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TextComponent.Builder
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Nameable
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.awt.Color
import java.util.*

class Text(val string: String = "", val color: TextColor? = null, vararg decorations: TextDecoration) : Audience {

    enum class EnumOption {
        SPACES, TITLE_CASE, LOWERCASE, NO_ACCENT
    }

    companion object {
        val newline = Text("\n")
        val space = Text(" ")

        fun newline(audience: Audience) {
            audience.sendMessage(Component.newline())
        }

        fun broadcastNewLine(permission: String? = null) {
            BlueFox.server.apply {
                permission?.let { broadcast(Component.newline(), it) }
            }
        }

        fun list(items: List<String>, separator: String = "\n", itemColor: String = "accent", textColor: String = "text", prefix: String = "", postfix: String = ""): Text {
            if(items.isEmpty()) return Text()
            val result = Text(prefix, textColor)
            for ((i, item) in items.withIndex()) {
                result += Text(item, itemColor)
                if(i == items.size - 1) break
                result += Text(separator, textColor)
            }
            return result + Text(postfix, textColor)
        }

        fun String.titleCase(delimiter: String): String {
            return split(delimiter).joinToString(delimiter) { word ->
                word.lowercase().replaceFirstChar { char -> char.uppercaseChar()}
            }
        }

        fun String.titleCase(vararg delimiters: String): String {
            var string = this
            for(delimiter in delimiters) {
                string = string.titleCase(delimiter)
            }
            return string
        }

        operator fun Component.plus(text: Text): Component {
            return this.append(text.component)
        }

        operator fun Component.plus(component: Component): Component {
            return this.append(component)
        }

        operator fun Component.plus(builder: TextComponent.Builder): Component = append(builder)

        operator fun Component.plusAssign(text: Text) {
            this.append(text.component)
        }

        operator fun Component.plusAssign(component: Component) {
            this.append(component)
        }

        operator fun Component.plusAssign(builder: TextComponent.Builder) {
            this.append(builder)
        }

        operator fun TextComponent.Builder.plus(text: Text): TextComponent.Builder {
            return this.append(text.component)
        }

        operator fun TextComponent.Builder.plus(component: Component): TextComponent.Builder {
            return this.append(component)
        }

        operator fun TextComponent.Builder.plus(builder: TextComponent.Builder): TextComponent.Builder {
            return this.append(builder)
        }

        operator fun TextComponent.Builder.plusAssign(text: Text) {
            this.append(text.component)
        }

        operator fun TextComponent.Builder.plusAssign(component: Component) {
            this.append(component)
        }

        operator fun TextComponent.Builder.plusAssign(builder: TextComponent.Builder) {
            this.append(builder)
        }
    }

    constructor(string: String, color: Int, vararg decorations: TextDecoration) : this(string, TextColor.color(color), *decorations)
    constructor(string: String, color: Color, vararg decorations: TextDecoration) : this(string, TextColor.color(color.rgb), *decorations)
    constructor(string: String, color: String, bedrock: Boolean = false, vararg decorations: TextDecoration) : this(
        string,
        ColorUtil.getColor(color, bedrock),
        *decorations
    )
    constructor(vararg options: EnumOption) : this("") {
        enumOptions = options
    }

    private val builder = Component.text(string).color(color).toBuilder().decorate(*decorations)
    var enumOptions: Array<out EnumOption> = arrayOf()

    fun enumOptions(vararg options: EnumOption) {
        this.enumOptions = options
    }

    fun suggest(command: String): Text {
        builder.clickEvent(ClickEvent.suggestCommand(command))
        return this
    }

    fun run(command: String): Text {
        builder.clickEvent(ClickEvent.runCommand(command))
        return this
    }

    fun hover(text: Text): Text {
        builder.hoverEvent(HoverEvent.showText(text.component))
        return this
    }

    fun hover(string: String, color: TextColor? = null, vararg decorations: TextDecoration): Text {
        builder.hoverEvent(HoverEvent.showText(Text(string, color, *decorations).component))
        return this
    }

    fun hover(string: String, color: String? = null, bedrock: Boolean = false, vararg decorations: TextDecoration): Text {
        return hover(string, color?.let { ColorUtil.getColor(it, bedrock) }, *decorations)
    }

    fun url(url: String): Text {
        builder.clickEvent(ClickEvent.openUrl(url))
        return this
    }

    fun color(color: TextColor): Text {
        builder.color(color)
        return this
    }

    fun color(colorName: String, bedrock: Boolean = false): Text {
        builder.color(ColorUtil.getColor(colorName, bedrock))
        return this
    }

    fun decorate(vararg decoration: TextDecoration): Text {
        builder.decorate(*decoration)
        return this
    }

    fun bold(): Text{
        builder.decorate(TextDecoration.BOLD)
        return this
    }

    fun italic(): Text{
        builder.decorate(TextDecoration.ITALIC)
        return this
    }

    fun boldItalic(): Text{
        builder.decorate(TextDecoration.BOLD, TextDecoration.ITALIC)
        return this
    }

    fun strikethrough(): Text{
        builder.decorate(TextDecoration.STRIKETHROUGH)
        return this
    }

    fun underlined(): Text {
        builder.decorate(TextDecoration.UNDERLINED)
        return this
    }

    fun obfuscated(): Text {
        builder.decorate(TextDecoration.OBFUSCATED)
        return this
    }

    private fun fillStart() {
        if(builder.children().isEmpty()) builder.append(Component.empty())
    }

    operator fun plus(text: Text): Text {
        fillStart()
        builder.append(text.component)
        return this
    }

    operator fun plusAssign(text: Text) {
        fillStart()
        builder.append(text.component)
    }

    operator fun plus(component: Component): Text {
        fillStart()
        builder.append(component)
        return this
    }

    operator fun plusAssign(component: Component) {
        fillStart()
        builder.append(component)
    }

    operator fun plus(builder: Builder): Text {
        fillStart()
        builder.append(builder)
        return this
    }

    operator fun plus(number: Number): Text {
        fillStart()
        return this + Text(number.toString(), "number")
    }

    operator fun <E : Enum<E>> plus(enum: E): Text {
        fillStart()
        var name = enum.toString()
        var color = "accent"
        for(enumOption in enumOptions.sortedBy { it.ordinal }) {
            when(enumOption) {
                EnumOption.LOWERCASE -> name = name.lowercase()
                EnumOption.TITLE_CASE -> name = name.titleCase("_", " ")
                EnumOption.SPACES -> name = name.replace("_", " ")
                EnumOption.NO_ACCENT -> color = "text"
            }
        }
        return this + Text(name, color)
    }

    operator fun plus(sender: CommandSender): Text {
        fillStart()
        return this + Text(sender.name, "player")
    }

    operator fun plus(itemStack: ItemStack): Text {
        fillStart()
        return this + itemStack.displayName()
    }

    operator fun plus(player: Player): Text {
        fillStart()
        return this + player.displayName()
    }

    operator fun plus(user: User): Text {
        fillStart()
        return this + user.displayName
    }

    operator fun plus(entity: Entity): Text {
        fillStart()
        return this + entity.name()
    }

    operator fun plus(string: String): Text {
        return this + Text(string)
    }

    operator fun plusAssign(builder: Builder) {
        fillStart()
        builder.append(builder)
    }


    operator fun inc(): Text {
        fillStart()
        builder.append(this.component)
        return this
    }

    operator fun times(int: Int): Text {
        fillStart()
        val component = this.component
        for(x in 0..int) builder.append(component)
        return this
    }

    fun send(audience: Audience): Text {
        audience.sendMessage(component)
        return this
    }

    fun broadcast(permission: String? = null): Text {
        BlueFox.server.apply {
            permission?.let { broadcast(component, it) } ?: broadcast(component)
        }
        return this
    }

    val component: Component get() = builder.build()
}
