package co.akoot.plugins.bluefox.util

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.XYZ
import co.akoot.plugins.bluefox.util.Text.Companion.noShadow
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TextComponent.Builder
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.json.JSONOptions.ShadowColorEmitMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.awt.Color

class Text(val string: String = "", val color: TextColor? = null, vararg decorations: TextDecoration) {

    enum class EnumOption {
        SPACES, TITLE_CASE, LOWERCASE, NO_ACCENT
    }

    companion object {
        val newline = Text("\n")
        val space = Text(" ")

        fun accent(string: String, bedrock: Boolean = false): Text {
            return Text(string, "accent", bedrock)
        }

        fun player(string: String, bedrock: Boolean = false): Text {
            return Text(string, "accent", bedrock)
        }

        fun number(string: String, bedrock: Boolean = false): Text {
            return Text(string, "number", bedrock)
        }

        fun error(sender: CommandSender, bedrock: Boolean = false, erm: (Text) -> Text) {
            erm(Text("", "error_text", bedrock)).send(sender)
        }

        fun newline(audience: Audience) {
            audience.sendMessage(Component.newline())
        }

        fun broadcastNewLine(permission: String? = null) {
            BlueFox.server.apply {
                permission?.let { broadcast(Component.newline(), it) }
            }
        }

        fun broadcast(permission: String? = null, erm: (Text) -> Text) {
            erm(Text()).broadcast(permission)
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

        fun String.noShadow(): Text {
            return Text(this).noShadow()
        }

        fun String.color(colorName: String, shadowColor: String? = null, shadowAlpha: Double = 1.0, bedrock: Boolean = false): Text {
            val text = Text(this, colorName, bedrock)
            return if(shadowColor != null) text.shadowColor(shadowColor, shadowAlpha, bedrock) else text
        }

        fun String.color(color: TextColor): Text {
            return Text(this, color)
        }

        fun String.color(color: TextColor, shadowColor: ShadowColor): Text {
            return Text(this, color).shadowColor(shadowColor)
        }

        fun String.accented(bedrock: Boolean = false): Text {
            return Text(this).color("accent")
        }

        fun String.error(colorName: String? = "text", bedrock: Boolean = false): Text {
            return Text(this).color("error_$colorName")
        }

        fun String.errorAccented(bedrock: Boolean = false): Text {
            return Text(this).color("error_accent")
        }

        fun String.hover(text: Text): Text {
            return Text(this).hover(text)
        }

        fun String.hover(string: String, color: TextColor? = null): Text {
            return Text(this).hover(Text(string, color))
        }

        fun String.hover(string: String, colorName: String, bedrock: Boolean = false): Text {
            return Text(this).hover(Text(string, colorName, bedrock))
        }

        fun String.decorate(vararg decorations: TextDecoration): Text {
            return Text(this).decorate(*decorations)
        }

        fun String.copy(string: String = this): Text {
            return Text(this).copy(string)
        }

        fun String.bold(): Text {
            return Text(this).bold()
        }

        fun String.italic(): Text {
            return Text(this).italic()
        }

        fun String.boldItalic(): Text {
            return Text(this).boldItalic()
        }

        fun String.underlined(): Text {
            return Text(this).underlined()
        }

        fun String.strikethrough(): Text {
            return Text(this).strikethrough()
        }

        fun String.obfuscated(): Text {
            return Text(this).obfuscated()
        }

        fun String.open(url: String): Text {
            return Text(this).open(url)
        }

        fun String.execute(command: String): Text {
            return Text(this).execute(command)
        }

        fun String.suggest(command: String): Text {
            return Text(this).suggest(command)
        }

        fun Number.copy(): Text {
            return number(this.toString()).hover("Click to copy!", "accent").copy(this.toString())
        }

        operator fun String.invoke(color: String = "text", bedrock: Boolean = false): Text {
            return Text(this, color, bedrock)
        }

        operator fun Text.plus(any: Any): Text {
            builder.append(accent(any.toString()).component)
            return this
        }

        operator fun Component.plus(text: Text): Component {
            return this.append(text.component)
        }

        operator fun Component.plus(component: Component): Component {
            return this.append(component)
        }

        operator fun Component.plus(xyz: XYZ): Component {
            this.append(xyz.toComponent())
            return this
        }

        operator fun Builder.plus(xyz: XYZ): Builder {
            return this.append(xyz.toComponent())
        }

        operator fun Component.plusAssign(xyz: XYZ) {
            this.append(xyz.toComponent())
        }

        operator fun Builder.plusAssign(xyz: XYZ) {
            this.append(xyz.toComponent())
        }

        operator fun Component.plus(builder: Builder): Component = append(builder)

        operator fun Component.plusAssign(text: Text) {
            this.append(text.component)
        }

        operator fun Component.plusAssign(component: Component) {
            this.append(component)
        }

        operator fun Component.plusAssign(builder: Builder) {
            this.append(builder)
        }

        operator fun Builder.plus(text: Text): Builder {
            return this.append(text.component)
        }

        operator fun Builder.plus(component: Component): Builder {
            return this.append(component)
        }

        operator fun Builder.plus(builder: Builder): Builder {
            return this.append(builder)
        }

        operator fun Builder.plusAssign(text: Text) {
            this.append(text.component)
        }

        operator fun Builder.plusAssign(component: Component) {
            this.append(component)
        }

        operator fun Builder.plusAssign(builder: Builder) {
            this.append(builder)
        }
    }

    fun noShadow(): Text {
        builder.shadowColor(ColorUtil.TRANSPARENT)
        return this
    }

    fun shadowColor(colorName: String, alpha: Double = 1.0, bedrock: Boolean = false): Text {
        builder.shadowColor(ColorUtil.getShadowColor(colorName, alpha, bedrock))
        return this
    }

    fun shadowColor(shadowColor: ShadowColor): Text {
        builder.shadowColor(shadowColor)
        return this
    }
    fun copy(string: String): Text {
        builder.clickEvent(ClickEvent.copyToClipboard(string))
        return this
    }

    fun open(url: String): Text {
        builder.clickEvent(ClickEvent.openUrl(url))
        return this
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
    constructor(erm: (Text) -> Text): this() {
        erm(this)
    }
    constructor(sender: CommandSender, erm: (Text) -> Text): this() {
        (erm(this)).send(sender)
    }

    private val builder = Component.text(string).color(color).toBuilder().decorate(*decorations)
    private var enumOptions: Array<out EnumOption> = arrayOf()

    fun enumOptions(vararg options: EnumOption) {
        this.enumOptions = options
    }

    fun suggest(command: String): Text {
        builder.clickEvent(ClickEvent.suggestCommand(command))
        return this
    }

    fun execute(command: String): Text {
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

    operator fun plusAssign(xyz: XYZ) {
        builder.append(xyz.toComponent())
    }

    operator fun plus(xyz: XYZ): Text {
        builder.append(xyz.toComponent())
        return this
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
