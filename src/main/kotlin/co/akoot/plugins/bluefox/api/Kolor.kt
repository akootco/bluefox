package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.extensions.mix
import co.akoot.plugins.bluefox.util.ColorUtil
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.bluefox.util.invert
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.awt.Color

class Kolor(java: Int, bedrock: Int = java, char: Char? = null) {

    companion object {
        val TEXT = Kolor(0xfcf7f4, 0xFFFFFF)
        val ALT = Kolor(0xbf97fc, 0x5555FF)
        val ACCENT = Kolor(0x97d4fc, 0x55FFFF)
        val PLAYER = Kolor(0xfc97c9, 0xFF55FF)
        val NUMBER = Kolor(0xfce497, 0xFFFF55)
        val QUOTE = Kolor(0xb2aaa6, 0xaaaaaa)
        val ERROR = Kolor(0xfc5f5f, 0xFF5555)
        val WARNING = Kolor(0xfcae5f, 0xFFAA00)
        val MONTH = Kolor(ColorUtil.MONTH_COLOR)
        val map = mapOf(
            't' to TEXT,
            '2' to ALT,
            'a' to ACCENT,
            'p' to PLAYER,
            'n' to NUMBER,
            'q' to QUOTE,
            'e' to ERROR,
            'w' to WARNING,
            'm' to MONTH
        )
    }

    constructor(hex: String, char: Char? = null): this(Color.decode(hex).rgb)
    constructor(textColor: TextColor, char: Char? = null): this(textColor.value())

    val raw = TextColor.color(java)
    val bedrock = TextColor.color(bedrock)

    val text by lazy { ColorUtil.month(raw) }
    val quote by lazy { ColorUtil.mix(text, QUOTE.raw) }
    val error by lazy { ColorUtil.mix(text, ERROR.raw) }
    val warning by lazy { ColorUtil.mix(text, WARNING.raw) }

    val alt by lazy { ColorUtil.mix(ALT.text, raw) }
    val accent by lazy { ColorUtil.mix(ACCENT.text, raw) }
    val player by lazy { ColorUtil.mix(PLAYER.text, raw) }
    val number by lazy { ColorUtil.mix(NUMBER.text, raw) }

    operator fun invoke(any: Any, bedrock: Boolean = false, rawColor: Boolean = false): Text {
        return Text(any.toString(), get(bedrock, rawColor))
    }

    operator fun invoke(string: String, bedrock: Boolean = false, rawColor: Boolean = false): Text {
        return Text(string, get(bedrock, rawColor))
    }

    operator fun invoke(component: Component, bedrock: Boolean = false, rawColor: Boolean = false): Text {
        return Text(component).color(get(bedrock, rawColor))
    }

    operator fun plus(kolor: Kolor): Kolor {
        return Kolor(raw.mix(kolor.raw))
    }

    operator fun plus(textColor: TextColor): Kolor {
        return Kolor(raw.mix(textColor))
    }

    fun get(isBedrock: Boolean = false, rawColor: Boolean = false): TextColor {
        return if(isBedrock) bedrock else (if(rawColor) raw else text)
    }

    fun parse(string: String): TextColor? {
        var bedrock = false
        var raw = false
        var result: TextColor? = null

        for (char in string) {
            when (char) {
                'r' -> raw = true
                'b' -> bedrock = true
                else -> {
                    val kolor = map[char]?.get(bedrock, raw) ?: continue
                    result = if (result == null) kolor else ColorUtil.mix(result, kolor)
                }
            }
        }

        return result
    }


    val invert get() = text.invert
}