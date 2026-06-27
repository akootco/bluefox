package co.akoot.plugins.bluefox.util

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.extensions.mix
import co.akoot.plugins.bluefox.extensions.username
import co.akoot.plugins.bluefox.util.ColorUtil.randomColor
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.`object`.ObjectContents
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.ShulkerBox
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import java.math.BigDecimal
import java.text.DecimalFormat
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

fun CommandSender.sendText(vararg text: Any): Boolean {
    sendMessage(text(*text))
    return true
}

fun CommandSender.sendWarning(vararg warning: Any): Boolean {
    sendMessage(warning(*warning))
    return false
}

fun CommandSender.sendError(vararg error: Any): Boolean {
    sendMessage(error(*error))
    return false
}

fun Player.sendActionBar(components: MutableList<Component>, separator: Component? = null, color: TextColor? = null) {
    this.sendActionBar(components.join(separator, color) { it })
}

fun Player.sendActionBarText(vararg text: Any) = sendActionBar(text(*text))
fun Player.sendActionBarWarning(vararg text: Any) = sendActionBar(warning(*text))
fun Player.sendActionBarError(vararg text: Any) = sendActionBar(error(*text))

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
fun Component.hover(items: Array<ItemStack>, title: Component = Component.text("Contents")): Component {
    val item = ItemStack(Material.SHULKER_BOX)
    val meta = item.itemMeta as BlockStateMeta
    meta.displayName(title)
    val shulker = meta.blockState as ShulkerBox
    shulker.inventory.contents = items.copyOf(27)
    meta.blockState = shulker
    item.itemMeta = meta
    return this.hoverEvent(item)
}

/**
 * Texture atlases were split in snapshot `25w45a` :(
 * * Use `<namespace>:item/<name>` or `<namespace>:block/<name>`
 * * Note: The namespace is optional. but is required for custom textures.
 */
fun sprite(location: String): Component {
    val atlas = location.substringAfter(":").substringBefore("/")
    return Component.`object`(ObjectContents.sprite(Key.key("${atlas}s"), Key.key(location)))
}

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

private val df = DecimalFormat("#.################")

val BigDecimal.asCurrency: String get() = stripTrailingZeros().toPlainString()
val Double.asCurrency: String get() = df.format(this)

fun String.s(n: Number) = if(n == 1) this else "${this}s"

fun World.component(color: TextColor = Color.Secondary): Component {
    val envColor = when (environment) {
        World.Environment.NETHER -> TextColor.color(0xff0000)
        World.Environment.THE_END -> TextColor.color(0xff00ff)
        World.Environment.NORMAL -> TextColor.color(0x00ff00)
        else -> TextColor.color(0x000000)
    }
    return color.mix(envColor) + name
}

val Location.biome: Biome get() = block.biome
val Location.biomeName: Component get() = Component.translatable(block.biome.translationKey())

fun Location.toComponent(): Component = text(blockX, ", ", blockY, ", ", blockZ, " in ", world.component()).join("")

val Double.percent: String get() = String.format("%.2f", this * 100)

fun <T> Boolean.get(whenTrue: T, whenFalse: T) = if(this) whenTrue else whenFalse

//TODO: Replace get() with const and =
val colorCodes get() = mapOf(
    "4" to TextColor.color(0xAA0000),
    "c" to TextColor.color(0xFF5555),
    "6" to TextColor.color(0xFFAA00),
    "e" to TextColor.color(0xFFFF55),
    "2" to TextColor.color(0x00AA00),
    "a" to TextColor.color(0x55FF55),
    "b" to TextColor.color(0x55FFFF),
    "3" to TextColor.color(0x00AAAA),
    "1" to TextColor.color(0x0000AA),
    "9" to TextColor.color(0x5555FF),
    "d" to TextColor.color(0xFF55FF),
    "5" to TextColor.color(0xAA00AA),
    "f" to TextColor.color(0xFFFFFF),
    "7" to TextColor.color(0xAAAAAA),
    "8" to TextColor.color(0x555555),
    "0" to TextColor.color(0x000000),
    "cyan" to Color.Cyan,
    "magenta" to Color.Magenta,
    "yellow" to Color.Yellow,
    "black" to Color.Black,
    "white" to Color.White,
    "red" to Color.Red,
    "green" to Color.Green,
    "blue" to Color.Blue,
    "text" to Color.Text,
    "primary" to Color.Primary,
    "secondary" to Color.Secondary,
    "tertiary" to Color.Tertiary,
    "number" to Color.Number,
    "quote" to Color.Quote,
    "error" to Color.Error,
    "warning" to Color.Warning,
)

val palettes get() = mapOf(
    "rgb" to listOf(Color.Red, Color.Green, Color.Blue),
    "cmyk" to listOf(Color.Cyan, Color.Magenta, Color.Yellow, Color.Black),
)

fun stripColor(input: String): String = buildString {
    for (token in tokenize(input)) {
        when (token) {
            is Token.PlainText -> append(token.text)
            is Token.ClickableText -> append(token.title)
            is Token.Placeholder -> append($$"${$${token.path.joinToString(".")}}")
            is Token.ColorCode -> {} // drop
        }
    }
}

// Sealed type makes match kinds explicit and exhausting
sealed interface Token {
    data class ColorCode(val codes: String, val inverted: Boolean) : Token
    data class ClickableText(val title: String, val target: String) : Token
    data class Placeholder(val path: List<String>) : Token
    data class PlainText(val text: String) : Token
}

// Patterns kept separate and readable
private val HEX_COLOR     get() = """#[0-9a-f]{6}"""
private val PALETTE_COLOR get() = """(?:\w{2,}\d|\w{2,}[<*^#?>])"""
private val SIMPLE_CODE   get() = """[0-9a-fk-or]"""
private val X_CODE        get() = """x\d{0,3}(?:,\d{1,3}){0,2}"""
private val NAMED_COLOR   get() = """\w+(?:\.|:)"""

// A single color code segment (one code between & and the next & or text)
private val ONE_CODE get() = """(?:$HEX_COLOR|$PALETTE_COLOR|$NAMED_COLOR|$SIMPLE_CODE|$X_CODE)"""

// Full &code1+code2+...! pattern
private val COLOR_TOKEN   get() = Regex("""&($ONE_CODE(?:\+$ONE_CODE)*)(!)?""", RegexOption.IGNORE_CASE)
private val CLICK_TOKEN   get() = Regex("""\[([^]]+)]\(([^)]+)\)""")
private val HOLDER_TOKEN  get() = Regex("""\$\{(\w+(?:\.\.?\w+)+)}""")
private val URL_REGEX     get() = Regex("""https?://\S+""")

fun tokenize(input: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0

    while (i < input.length) {
        // Escape sequences: && [[ ]] $$ }}
        if (i + 1 < input.length) {
            val two = input.substring(i, i + 2)
            if (two in setOf("&&", "[[", "]]", "$$", "}}")) {
                tokens.addPlainText(two[0].toString()) // unescape
                i += 2
                continue
            }
        }

        // Try each token type at current position
        COLOR_TOKEN.matchAt(input, i)?.let {
            val codes = it.groupValues[1]
            val inverted = it.groupValues[2] == "!"
            tokens += Token.ColorCode(codes, inverted)
            i += it.value.length
            return@let
        } ?: CLICK_TOKEN.matchAt(input, i)?.let {
            tokens += Token.ClickableText(it.groupValues[1], it.groupValues[2])
            i += it.value.length
            return@let
        } ?: HOLDER_TOKEN.matchAt(input, i)?.let {
            tokens += Token.Placeholder(it.groupValues[1].split(Regex("\\.+?")))
            i += it.value.length
            return@let
        } ?: run {
            // Plain character — no pattern matched
            tokens.addPlainText(input[i].toString())
            i++
        }
    }
    return tokens
}

// Merge consecutive PlainText tokens to reduce allocations
private fun MutableList<Token>.addPlainText(char: String) {
    val last = lastOrNull()
    if (last is Token.PlainText) set(size - 1, Token.PlainText(last.text + char))
    else add(Token.PlainText(char))
}

// ---- Formatting state ----
private data class FormatState(
    val colors: List<TextColor?> = emptyList(),
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val magic: Boolean = false,
    val inverted: Boolean = false,
) {
    fun reset() = FormatState()
}

fun String.parse(tint: TextColor? = null): Component {
    val root = Component.text()
    var state = FormatState()

    for (token in tokenize(this)) {
        when (token) {
            is Token.ColorCode -> state = applyColorCode(token, state)

            is Token.PlainText -> {
                if (token.text.isBlank() && state.colors.isEmpty()) {
                    root.append(Component.text(token.text))
                } else {
                    val cols = if (state.inverted) state.colors.mapNotNull { it?.inverted } else state.colors
                    var comp = color(token.text, *cols.toTypedArray())
                    if (state.bold)          comp = comp.decorate(TextDecoration.BOLD)
                    if (state.italic)        comp = comp.decorate(TextDecoration.ITALIC)
                    if (state.underline)     comp = comp.decorate(TextDecoration.UNDERLINED)
                    if (state.strikethrough) comp = comp.decorate(TextDecoration.STRIKETHROUGH)
                    if (state.magic)         comp = comp.decorate(TextDecoration.OBFUSCATED)
                    root.append(comp)
                }
                state = state.copy(colors = emptyList(), inverted = false)
            }

            is Token.ClickableText -> {
                root.append(buildClickable(token, tint))
            }

            is Token.Placeholder -> {
                resolvePlaceholder(token.path)?.let { root.append(it) }
            }
        }
    }

    return root.build()
}

private fun applyColorCode(token: Token.ColorCode, state: FormatState): FormatState {
    val colors = state.colors.toMutableList()
    var bold = state.bold; var italic = state.italic
    var underline = state.underline; var strike = state.strikethrough; var magic = state.magic

    for (code in token.codes.split("+")) {
        when {
            code.startsWith("#") -> colors += TextColor.fromHexString(code)
            code.startsWith("x") -> colors += parseXColor(code)
            code.last() in setOf(':', '.') -> colors += colorCodes[code.dropLast(1)]
            code in palettes -> colors += palettes[code] ?: emptyList()
            code.matches(Regex(PALETTE_COLOR, RegexOption.IGNORE_CASE)) -> {
                val m = Regex("""(\w+?)(\d+|[<*^#?>])""").matchEntire(code) ?: continue
                val name = m.groupValues[1]; val modifier = m.groupValues[2]
                val cols = palettes[name] ?: continue
                val index = modifier.toIntOrNull()
                if (index != null) {
                    colors += cols.getOrNull(index)
                } else {
                    colors.addAll(when (modifier) {
                        "<" -> cols.reversed()
                        "*" -> cols + cols.reversed().drop(1)
                        "^" -> cols.reversed() + cols.drop(1)
                        "#" -> cols.shuffled()
                        "?" -> listOf(cols.random())
                        else -> cols
                    })
                }
            }
            code in colorCodes -> colors += colorCodes[code]
            code == "l" -> bold = true
            code == "o" -> italic = true
            code == "n" -> underline = true
            code == "m" -> strike = true
            code == "k" -> magic = true
            code == "r" -> return FormatState() // hard reset
        }
    }

    return state.copy(
        colors = colors,
        bold = bold, italic = italic,
        underline = underline, strikethrough = strike,
        magic = magic, inverted = token.inverted
    )
}

private fun parseXColor(code: String): TextColor {
    val args = code.drop(1).split(",")
    val y  = args.getOrNull(0)?.toIntOrNull() ?: 100
    val z1 = args.getOrNull(1)?.toIntOrNull() ?: 0
    val z2 = args.getOrNull(2)?.toIntOrNull() ?: 360
    val sat = 1f - (if (y > 100) y.coerceAtMost(199) % 100 / 100f else 0f)
    val bri = y.coerceAtMost(100) / 100f
    return randomColor(sat, bri, z1 / 360f, z2 / 360f)
}

private fun buildClickable(token: Token.ClickableText, tint: TextColor?): Component {
    val stripped = stripColor(token.target).replace("&&", "&")
    val hover = token.target.parse(tint)
    var comp = token.title.parse(tint).hoverEvent(HoverEvent.showText(hover))
    if (stripped.isNotBlank() && token.target != "-") {
        comp = when {
            URL_REGEX.matches(stripped)     -> comp.clickEvent(ClickEvent.openUrl(stripped))
            stripped.startsWith("!/")       -> comp.clickEvent(ClickEvent.runCommand(stripped.drop(1)))
            stripped.startsWith("/")        -> comp.clickEvent(ClickEvent.suggestCommand(stripped))
            stripped.startsWith("+")        -> comp.clickEvent(ClickEvent.copyToClipboard(stripped.drop(1)))
            stripped.startsWith("^")        -> comp.clickEvent(ClickEvent.openFile(stripped.drop(1)))
            else -> comp
        }
    }
    return comp
}

private fun resolvePlaceholder(args: List<String>): Component? {
    return when (args[0]) {
        "player" -> {
            val shift = if (args[1].isBlank()) 1 else 0
            val user = BlueFox.getPlayer(args[1].ifBlank { ".${args[2]}" })
            val player = user?.player ?: return null
            when (args[2 + shift]) {
                "item"        -> player.inventory.itemInMainHand.displayName()
                "item2"       -> player.inventory.itemInOffHand.displayName()
                "hat"         -> (player.inventory.armorContents.lastOrNull() ?: ItemStack(Material.AIR)).displayName()
                "displayName" -> user.displayName()
                else          -> null
            }
        }
        "server" -> when (args[1]) {
            "seed" -> Component.text("[").color(TextColor.color(0xffffff))
                .append(Component.text("obamnasoda").color(TextColor.color(0x55FF55)))
                .append(Component.text("]")).color(TextColor.color(0xffffff))
            else -> null
        }
        else -> null
    }
}

fun color(text: String, vararg colors: TextColor?): TextComponent {

    if (colors.isEmpty()) return Component.text(text)
    if (colors.size == 1) {
        val color = colors[0]
        return Component.text(text).color(if (color == null) null else TextColor.color(color))
    }

    val noSpaces = text.replace(" ", "")
    val gradient = ColorUtil.getGradient(noSpaces.length, *colors)
    val component = Component.text()
    var j = 0
    for (c in text) {
        if (c != ' ') {
            component.append(Component.text(c).color(gradient[j]))
            j++
        } else {
            component.append(Component.text(" "))
        }
    }
    return component.build()
}
