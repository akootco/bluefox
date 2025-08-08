package co.akoot.plugins.bluefox.util

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.api.XYZ
import co.akoot.plugins.bluefox.extensions.invoke
import co.akoot.plugins.bluefox.extensions.isBedrock
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TextComponent.Builder
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import net.kyori.adventure.translation.Translatable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.awt.Color
import java.math.BigDecimal
import java.net.URLEncoder
import java.time.Duration

class Text(val string: String = "", val color: TextColor? = null, val bedrock: Boolean = false, val rawColor: Boolean = false, vararg decorations: TextDecoration) {

    enum class EnumOption {
        SPACES, TITLE_CASE, LOWERCASE, NO_ACCENT
    }

    companion object {
        val newline = Text("\n")
        val space = Text(" ")

        val Boolean.now: String get() = if(this) "now" else "no longer"
        val Boolean.not: String get() = if(this) "is" else "is not"
        val Boolean.yes: String get() = if(this) "yes" else "no"
        val Boolean.enabled: String get() = if(this) "enabled" else "disabled"
        val Boolean.on: String get() = if(this) "on" else "off"

        fun Boolean.get(whetherTrue: String, whetherFalse: String): String {
            return if(this) whetherTrue else whetherFalse
        }

        fun list(items: List<String>, separator: String = "\n", itemKolor: Kolor = Kolor.ACCENT, textKolor: Kolor = Kolor.TEXT, prefix: String = "", postfix: String = "", bedrock: Boolean = false, rawColor: Boolean = false): Text {
            if(items.isEmpty()) return Text()
            val textColor = textKolor.get(bedrock, rawColor)
            val itemColor = itemKolor.get(bedrock, rawColor)
            val result = Text(prefix, textColor)
            for ((i, item) in items.withIndex()) {
                result += Text(item, itemColor)
                if(i == items.size - 1) break
                result += Text(separator, textColor)
            }
            return result + Text(postfix, textColor)
        }

        operator fun TextColor.plus(string: String): Text {
            return Text(string).color(this)
        }

        operator fun Kolor.plus(string: String): Text {
            return Text(string).color(this.text)
        }

        fun String.titleCase(delimiter: String): String {
            return split(delimiter).joinToString(delimiter) { word ->
                word.lowercase().replaceFirstChar { char -> char.uppercaseChar()}
            }
        }

//        val clickableTextRegex = Regex("")
//        val placeholderRegex = Regex("")
//        val colorRegex = Regex("")
//        val parseRegex = Regex("($clickableTextRegex)|($placeholderRegex)|($colorRegex)|([^&\\[\\]\$}]*(?:(?:\\[\\[|&&|\\\$\\\$|}}|]])[^&]*)?)", RegexOption.IGNORE_CASE)
//        fun String.parse(): Text {
//            val colors = mutableListOf<TextColor?>()
//            val bold = false
//            val italic = false
//            val strikethrough = false
//            val magic = false
//            val inverted = false
//        }

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

        operator fun String.invoke(kolor: Kolor = Kolor.TEXT, bedrock: Boolean = false, rawColor: Boolean = false): Text {
            return Text(this, kolor.get(bedrock, rawColor))
        }

        fun Number.copy() {
            Kolor.TEXT(this.toString()).copy(this.toString())
        }

        val Number.text: Text get() {
            val parts = this.toString().split(".")
            if (parts.size == 1) return Kolor.NUMBER(parts[0])
            return Kolor.NUMBER(parts[0]) + Kolor.QUOTE(".${parts[1]}")
        }

        val BigDecimal.text: Text get() {
            val parts = this.toPlainString().split(".")
            if (parts.size == 1) return Kolor.NUMBER(parts[0])
            return Kolor.NUMBER(parts[0]) + Kolor.QUOTE(".${parts[1]}")
        }

        val Material.component: Component get() = Component.translatable(translationKey())
        val Material.text: Text get() = Text(component)

        fun Material.text(color: TextColor): Text {
            return Text(component).color(color)
        }

        operator fun Component.plus(number: Number): Component {
            return this.append(number.text.component)
        }

        operator fun Component.plus(string: String): Component {
            return this.append(string().component)
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

        operator fun Component.plus(builder: Builder): Component {
            return append(builder)
        }

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

        fun Component.asString(): String {
            return PlainTextComponentSerializer.plainText().serialize(this)
        }

        val Material.cleanName: String get() = this.name.lowercase().replace("_", "")

        fun translate(text: String, sourceLang: String = "auto", targetLang: String = "en"): String {
            val client = OkHttpClient()
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val url = "https://lingva.ml/api/v1/$sourceLang/$targetLang/$encodedText"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("Translation failed: ${response.code}")
                    return text
                }

                val json = response.body?.string()
                val startIndex = json?.indexOf("\"translation\":\"") ?: return text
                val endIndex = json.indexOf("\"", startIndex + 15)
                return json.substring(startIndex + 15, endIndex)
            }
        }
    }

    constructor(string: String, color: Int, vararg decorations: TextDecoration) : this(string, TextColor.color(color), false, true, *decorations)
    constructor(string: String, color: Color, vararg decorations: TextDecoration) : this(string, TextColor.color(color.rgb), false, true, *decorations)
    constructor(string: String, kolor: Kolor, bedrock: Boolean = false, rawColor: Boolean = false, vararg decorations: TextDecoration) : this(
        string,
        kolor.get(bedrock, rawColor),
        bedrock,
        rawColor,
        *decorations
    )
    constructor(vararg options: EnumOption) : this("") {
        enumOptions = options
    }
    constructor(erm: (Text) -> Text): this("") {
        erm(this)
    }
    constructor(sender: CommandSender?, erm: (Text) -> Text): this("", bedrock = sender is Player && sender.isBedrock) {
        if(sender == null) return
        val text = erm(this)
        text.send(sender)
    }

    constructor(component: Component) : this("") {
        builder.append(component)
    }

    private val builder = Component.text(string).color(color).toBuilder().decorate(*decorations)
    private var enumOptions: Array<out EnumOption> = arrayOf()
    val json: String get() = JSONComponentSerializer.json().serialize(component)

    fun noShadow(): Text {
        builder.shadowColor(ColorUtil.TRANSPARENT)
        return this
    }

    fun shadowColor(kolor: Kolor, alpha: Double = 1.0): Text {
        builder.shadowColor(kolor.get(bedrock, rawColor).toShadowColor(alpha))
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
        builder.hoverEvent(HoverEvent.showText(Text(string, color, bedrock = false, rawColor = true, *decorations).component))
        return this
    }

    fun hover(itemStack: ItemStack): Text {
        builder.hoverEvent(itemStack)
        return this
    }

    fun hover(entity: Entity): Text {
        builder.hoverEvent(entity)
        return this
    }

    fun url(url: String): Text {
        builder.clickEvent(ClickEvent.openUrl(url))
        return this
    }

    fun color(color: TextColor): Text {
        builder.color(color)
        return this
    }

    fun color(kolor: Kolor): Text {
        builder.color(kolor.get(bedrock, rawColor))
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

    operator fun plus(any: Any): Text {
        builder.append(Kolor.ACCENT(any.toString()).component)
        return this
    }

    operator fun plus(text: Text): Text {
        builder.append(text.component)
        return this
    }

    operator fun plusAssign(text: Text) {
        builder.append(text.component)
    }

    operator fun plus(component: Component): Text {
        builder.append(component)
        return this
    }

    operator fun plusAssign(component: Component) {
        builder.append(component)
    }

    operator fun plus(builder: Builder): Text {
        builder.append(builder)
        return this
    }

    operator fun plus(number: Number): Text {
        return this + number.text
    }

    operator fun <E : Enum<E>> plus(enum: E): Text {
        var name = enum.toString()
        var kolor = Kolor.ACCENT
        for(enumOption in enumOptions.sortedBy { it.ordinal }) {
            when(enumOption) {
                EnumOption.LOWERCASE -> name = name.lowercase()
                EnumOption.TITLE_CASE -> name = name.titleCase("_", " ")
                EnumOption.SPACES -> name = name.replace("_", " ")
                EnumOption.NO_ACCENT -> kolor = Kolor.TEXT
            }
        }
        return this + Text(name, kolor)
    }

    operator fun plus(sender: CommandSender): Text {
        return this + Text(sender.name, Kolor.PLAYER)
    }

    operator fun plus(itemStack: ItemStack): Text {
        return this + itemStack.displayName()
    }

    operator fun plus(player: Player): Text {
        return this + player.displayName()
    }

    operator fun plus(entity: Entity): Text {
        return this + entity.name()
    }

    operator fun plus(string: String): Text {
        return this + Text(string)
    }

    operator fun plusAssign(builder: Builder) {
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
        builder.append(this.component)
        return this
    }

    operator fun times(int: Int): Text {
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

    fun sendTitle(audience: Audience, subtitle: Text? = null, fadeIn: Double = 0.5, stay: Double = 3.0, fadeOut: Double = 0.5): Text {
        val times = Title.Times.times(
            Duration.ofMillis((fadeIn * 1000).toLong()), // fade in
            Duration.ofMillis((stay * 1000).toLong()), // stay
            Duration.ofMillis((fadeOut * 1000).toLong())  // fade out
        )
        audience.sendTitlePart(TitlePart.TIMES, times)
        subtitle?.let { audience.sendTitlePart(TitlePart.SUBTITLE, it.component) }
        audience.sendTitlePart(TitlePart.TITLE, component)
        return this
    }

    fun sendSubtitle(audience: Audience, fadeIn: Double = 0.5, stay: Double = 3.0, fadeOut: Double = 0.5): Text {
        val times = Title.Times.times(
            Duration.ofMillis((fadeIn * 1000).toLong()), // fade in
            Duration.ofMillis((stay * 1000).toLong()), // stay
            Duration.ofMillis((fadeOut * 1000).toLong())  // fade out
        )
        audience.sendTitlePart(TitlePart.SUBTITLE, component)
        audience.sendTitlePart(TitlePart.TITLE, Component.empty())
        return this
    }

    fun sendActionBar(player: Player): Text {
        player.sendActionBar(component)
        return this
    }

    val component: Component get() = builder.build()
}
