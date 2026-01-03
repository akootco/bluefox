package co.akoot.plugins.bluefox.util

import co.akoot.plugins.bluefox.extensions.mix
import co.akoot.plugins.bluefox.extensions.username
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.`object`.ObjectContents
import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.Calendar
import java.util.UUID

object Color {
    val Cyan = TextColor.color(0x00ffff)
    val Magenta = TextColor.color(0xff00ff)
    val Yellow = TextColor.color(0xffff00)
    val Black = TextColor.color(0)
    val White = TextColor.color(0xffffff)
    val Red = TextColor.color(0xff0000)
    val Green = TextColor.color(0x00ff00)
    val Blue = TextColor.color(0x0000ff)

    val Text = TextColor.color(0xfcf7f4)
    val Primary = TextColor.color(0x97d4fc)
    val Secondary = TextColor.color(0xbf97fc)
    val Tertiary = TextColor.color(0xfbae77)
    val Number = TextColor.color(0xfce497)
    val Quote = TextColor.color(0xb2aaa6)
    val Error = TextColor.color(0xfc5f5f)
    val Warning = TextColor.color(0xfcae5f)

    val Month  = when (Calendar.getInstance().get(Calendar.MONTH)) {
        Calendar.JANUARY -> TextColor.color(0x77BBE9)
        Calendar.FEBRUARY -> TextColor.color(0xFC81B0)
        Calendar.MARCH -> TextColor.color(0x44E881)
        Calendar.APRIL -> TextColor.color(0xB198FC)
        Calendar.MAY -> TextColor.color(0x8DD232)
        Calendar.JUNE -> TextColor.color(0xf9ba23)
        Calendar.JULY -> TextColor.color(0xfca873)
        Calendar.AUGUST -> TextColor.color(0xfbd17a)
        Calendar.SEPTEMBER -> TextColor.color(0x86aefc)
        Calendar.OCTOBER -> TextColor.color(0xfc9449)
        Calendar.NOVEMBER -> TextColor.color(0xdd9d78)
        Calendar.DECEMBER -> TextColor.color(0xfc4b55)
        else -> TextColor.color(0xffffff)
    }
}

fun Component.tint(color: TextColor?): Component {
    return if(color == null) {
        this.color(null)
    } else {
        this.color(this.color()?.mix(color) ?: color)
    }
}

fun <T> tint(color: TextColor, vararg components: T): MutableList<Component> {
    val list = components.map {
        when (it) {
            is Component -> it
            is Number -> Color.Number + it.toString()
            is Boolean -> Color.Tertiary + it.toString()
            is String -> Component.text(it)
            is Player -> it.displayName().colorIfAbsent(Color.Secondary)
            is OfflinePlayer -> Color.Secondary + it.username
            is Entity -> it.customName()?.colorIfAbsent(Color.Secondary) ?: (Color.Secondary + it.name)
            else -> Component.text(it.toString())
        }
    }.toMutableList()
    return tint(color, list)
}

fun tint(color: TextColor, components: MutableList<Component>): MutableList<Component> {
    val mixedComponents = mutableListOf<Component>()
    for(component in components) {
        mixedComponents += component.tint(color)
    }
    return mixedComponents
}

fun <T> error(vararg text: T): MutableList<Component> = tint(Color.Error, *text)
fun <T> quote(vararg text: T): MutableList<Component> = tint(Color.Quote, *text)
fun <T> text(vararg text: T): MutableList<Component> = tint(Color.Text, *text)
fun <T> warning(vararg text: T): MutableList<Component> = tint(Color.Warning, *text)

fun error(string: String): Component = Color.Error + string
fun quote(string: String): Component = Color.Quote + string
fun text(string: String): Component = Color.Text + string
fun warning(string: String): Component = Color.Warning + string

fun primary(any: Any?): Component = Color.Primary + any.toString()
fun secondary(any: Any?): Component = Color.Secondary + any.toString()
fun tertiary(any: Any?): Component = Color.Tertiary + any.toString()

// semantics
fun accent(any: Any?): Component = primary(any)
fun alt(any: Any?): Component = tertiary(any)
fun player(any: Any?): Component = secondary(any)
fun player1(any: Any?): Component = player(any)
fun player2(any: Any?): Component = Color.Secondary + player(any)

operator fun TextColor?.plus(color: TextColor): TextColor = this?.mix(color) ?: color
operator fun TextColor?.plus(component: Component): Component = component.tint(this)
operator fun TextColor?.plus(string: String): Component = Component.text(string).color(this)

fun MutableList<Component>.join(separator: Component?, tint: TextColor? = null, mutation: (Component) -> Component = { it }): Component {
    val builder = Component.text()
    if(separator == null) return builder.append(tint?.let { color -> this.map { it.tint(color) } } ?: this).build()
    for((i, c) in this.withIndex()) {
        val mutated = mutation(c)
        builder.append(tint?.let { mutated.tint(it) } ?: mutated )
        if(i < this.size - 1) builder.append(separator)
    }
    return builder.build()
}

fun MutableList<Component>.join(separator: String = " "): Component = this.join(Component.text(separator))

fun CommandSender.sendMessage(components: MutableList<Component>, separator: Component? = null, color: TextColor? = null) {
    this.sendMessage(components.join(separator, color) { it })
}

fun CommandSender.sendList(components: MutableList<Component>, separator: Component = Component.newline(), color: TextColor? = null,  mutator: (Component) -> Component = { it }) {
    this.sendMessage(components.join(separator, color, mutator))
}

fun CommandSender.sendText(vararg text: Any) = sendMessage(text(*text))
fun CommandSender.sendWarning(vararg warning: Any) = sendMessage(warning(*warning))
fun CommandSender.sendError(vararg error: Any) = sendMessage(error(*error))

operator fun Component.div(component: Component): MutableList<Component> = mutableListOf(this, Component.newline(), component)

fun Component.bold(bold: Boolean = true): Component = this.decoration(TextDecoration.BOLD, bold)
fun Component.italic(italic: Boolean = true): Component = this.decoration(TextDecoration.ITALIC, italic)
fun Component.underline(underline: Boolean = true): Component = this.decoration(TextDecoration.UNDERLINED, underline)
fun Component.obfuscated(obfuscated: Boolean = true): Component = this.decoration(TextDecoration.OBFUSCATED, obfuscated)
fun Component.strikethrough(strikethrough: Boolean = true): Component = this.decoration(TextDecoration.STRIKETHROUGH, strikethrough)
fun Component.boldItalic(bold: Boolean = true, italic: Boolean = bold): Component = this
    .decoration(TextDecoration.BOLD, bold)
    .decoration(TextDecoration.ITALIC, italic)

fun Component.hover(component: Component): Component = this.hoverEvent(HoverEvent.showText(component))
fun Component.hover(string: String): Component = this.hoverEvent(HoverEvent.showText(Component.text(string)))
fun Component.hover(itemStack: ItemStack): Component = this.hoverEvent(itemStack)

fun sprite(key: Key): Component = Component.`object`(ObjectContents.sprite(key))
fun sprite(location: String): Component = sprite(NamespacedKey.minecraft(location))

fun head(name: String): Component = Component.`object`(ObjectContents.playerHead(name))
fun head(uuid: UUID): Component = Component.`object`(ObjectContents.playerHead(uuid))
fun head(offlinePlayer: OfflinePlayer): Component = Component.`object`(ObjectContents.playerHead(offlinePlayer.uniqueId))

fun keybind(keybind: String): Component = Component.keybind(keybind)
fun translatable(translatable: String): Component = Component.translatable(translatable)

fun clickable(string: String, color: TextColor = Color.Tertiary + Color.Month, underline: Boolean = true, onClick: (Audience) -> Unit): Component {
    return clickable(string, color, underline, ClickEvent.callback { onClick(it) })
}

fun clickable(string: String, color: TextColor = Color.Tertiary + Color.Month, underline: Boolean = true, clickEvent: ClickEvent): Component {
    return (color + string)
        .underline(underline)
        .clickEvent(clickEvent)
}

fun execute(command: String): ClickEvent = ClickEvent.runCommand(command)
fun suggest(command: String): ClickEvent = ClickEvent.suggestCommand(command)
fun open(url: String): ClickEvent = ClickEvent.openUrl(url)
fun copy(text: String): ClickEvent = ClickEvent.copyToClipboard(text)
