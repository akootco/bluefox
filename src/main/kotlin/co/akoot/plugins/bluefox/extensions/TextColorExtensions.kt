package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.util.Color
import co.akoot.plugins.bluefox.util.ColorUtil
import co.akoot.plugins.bluefox.util.Text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

fun TextColor.brighten(percent: Double = 0.1): TextColor {
    return ColorUtil.brighten(this.value(), percent)
}

fun TextColor.darken(percent: Double = 0.1): TextColor {
    return ColorUtil.darken(this.value(), percent)
}

fun TextColor.hex(lowercase: Boolean = true): String {
    return ColorUtil.getHexString(this, lowercase)
}

fun TextColor.isGray(tolerance: Double = 0.1): Boolean {
    return ColorUtil.isGray(this.value(), (tolerance * 255).toInt())
}

fun TextColor.mix(color: TextColor?, mix: Double = 0.5): TextColor {
    if (color == null) return ColorUtil.mix(Color.White, this, mix)
    return ColorUtil.mix(this, color, mix)
}

fun TextColor.mix(kolor: Kolor, mix: Double = 0.5): TextColor {
    return ColorUtil.mix(this, kolor.raw, mix)
}

fun getTextColor(string: String): TextColor = TextColor.fromHexString(string) ?: Color.White
fun setTextColor(color: TextColor): String = color.hex()

operator fun TextColor.invoke(string: String): Text {
    return Text(string).color(this)
}

operator fun TextColor.invoke(number: Number): Text {
    return Text(number.toString()).color(this)
}

operator fun TextColor.invoke(component: Component): Text {
    return Text(component).color(this)
}