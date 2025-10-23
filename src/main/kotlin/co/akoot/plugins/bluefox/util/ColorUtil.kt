package co.akoot.plugins.bluefox.util

import co.akoot.plugins.bluefox.extensions.brighten
import co.akoot.plugins.bluefox.extensions.isGray
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import java.awt.Color
import java.awt.color.ColorSpace
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object ColorUtil {

    val WHITE = TextColor.color(0xffffff)
    val TRANSPARENT = ShadowColor.shadowColor(0x000000)

    val MONTH_COLOR = when (TimeUtil.MONTH) {
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

    fun textColor(nm: String): TextColor? {
        val color = runCatching {Color.decode(nm)}.getOrNull() ?: return null
        return TextColor.color(color.rgb)
    }

    fun month(color: TextColor, mix: Double = 0.25, brighten: Double = 0.15): TextColor {
        val result = mix(color, MONTH_COLOR, mix, 10)
        return if(brighten <= 0.0) result
        else result.brighten(brighten)
    }

    /**
     * Get the hue of a color
     *
     * @param color The color
     * @return The hue of a color in degrees
     */
    fun getHue(color: Int): Float {
        val col = Color(color)
        val min = col.red.coerceAtMost(col.green).coerceAtMost(col.blue).toFloat()
        val max = col.red.coerceAtLeast(col.green).coerceAtLeast(col.blue).toFloat()

        if (min == max) return 0f

        val hue: Float = when (max) {
            col.red.toFloat() -> (col.green - col.blue) / (max - min)
            col.green.toFloat() -> 2f + (col.blue - col.red) / (max - min)
            else -> 4f + (col.red - col.green) / (max - min)
        }

        return (hue * 60).let { if (it < 0) it + 360 else it }
    }

    fun getBrightness(color: Int): Float {
        val col = Color(color)
        val min = col.red.coerceAtMost(col.green).coerceAtMost(col.blue) / 255f
        val max = col.red.coerceAtLeast(col.green).coerceAtLeast(col.blue) / 255f
        return (max + min) / 2f
    }

    fun getSaturation(color: Int): Float {
        val col = Color(color)
        val r = col.red
        val g = col.green
        val b = col.blue
        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        if (max == min) return 0f
        val d = max - min
        return when (max) {
            r -> (g - b) / d + (if (g < b) 6f else 0f)
            g -> (b - r) / d + 2f
            b -> (r - g) / d + 4f
            else -> 0f
        } * 60f
    }

    /**
     * Check if a color is a shade of gray
     *
     * @param color The color
     * @param tolerance The threshold of what is considered "gray" (0-255).
     * Example: tolerance=10, r=100, g=110 b=90, isGray=true. If tolerance was 5, isGray=false
     * @return Whether a color is gray or not
     */
    fun isGray(color: Int, tolerance: Int = 10): Boolean {
        val col = Color(color)
        val rgDiff = col.red - col.green
        val rbDiff = col.red - col.blue
        if (rgDiff > tolerance || rgDiff < -tolerance) if (rbDiff > tolerance || rbDiff < -tolerance) return false
        return true
    }

    /**
     * Gets a hex string representation of a color
     *
     * @param color The color
     * @param lowercase Whether the hex string should be lowercase
     * @return A hex string representation of the color
     */
    fun getHexString(color: TextColor, lowercase: Boolean = true): String {
        return String.format("#%02x%02x%02x", color.red(), color.green(), color.blue())
            .let { if (lowercase) it.lowercase() else it.uppercase() }
    }

    /**
     * Lightens a color
     *
     * @param color The color to darken
     * @param percentage How dark it should be
     * @return A lighter color
     */
    fun brighten(color: Int, percentage: Double = 0.1): TextColor {
        val col = Color(color)
        val min = (255 * percentage.coerceAtMost(1.0)).toInt()
        return TextColor.color(
            (col.red + (col.red * percentage)).toInt().coerceAtMost(255).coerceAtLeast(min),
            (col.green + (col.green * percentage)).toInt().coerceAtMost(255).coerceAtLeast(min),
            (col.blue + (col.blue * percentage)).toInt().coerceAtMost(255).coerceAtLeast(min)
        )
    }

    /**
     * Darkens a color
     *
     * @param color The color to darken
     * @param percentage How dark it should be
     * @return A darker color
     */
    fun darken(color: Int, percentage: Double = 0.1): TextColor {
        val col = Color(color).darker()
        val factor = 1 - percentage
        return TextColor.color(
            (col.red * factor).toInt().coerceAtLeast(0),
            (col.green * factor).toInt().coerceAtLeast(0),
            (col.blue * factor).toInt().coerceAtLeast(0)
        )
    }

    /**
     * Get a random color
     *
     * @param saturation The desired saturation of the color
     * @param brightness The desired brightness of the color
     * @return A random color
     */
    fun randomColor(saturation: Float = 0.9f, brightness: Float = 0.9f): TextColor {
        return TextColor.color(Color.getHSBColor(Random.nextFloat(), saturation, brightness).rgb)
    }

    /**
     * Get a random color as a hex string
     *
     * @param saturation The desired saturation of the color
     * @param brightness The desired brightness of the color
     * @return A random color in the form of a hex string
     */
    fun randomColorHex(saturation: Float = 0.9f, brightness: Float = 0.9f): String {
        return getHexString(randomColor(saturation, brightness))
    }

    /**
     * Mix 2 colors
     *
     * @param color1 Color 1
     * @param color2 Color 2
     * @return The resulting color of mixing color1 and color2
     */
    fun mix(color1: TextColor, color2: TextColor, percentage: Double = 0.5, points: Int = 3): TextColor {
        return getGradient(points, color1, color2)[(points * percentage).toInt()]
    }

    private val cie = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ)
    private val sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB)

    /**
     * Generates a gradient based on the color points provided.
     * This method mixes the colors in the CIE color space, which
     * results in more vibrant colors
     *
     * @param size The number of colors to generate
     * @param points The colors to mix
     * @return A list of colors
     */
    fun getGradient(size: Int, vararg points: TextColor): MutableList<TextColor> {
        val gradient = ArrayList<TextColor>(size)

        val x = points.size
        val y = x - 1

        val a = size - x
        val b = a / y
        var c = a - (b * y)

        // precompute CIE XYZ for all points
        val ciePoints = Array(x) { i ->
            val rgb = Color(points[i].value()).getRGBColorComponents(null)
            cie.fromRGB(rgb)
        }

        // build gradient
        for (i in 0 until x) {
            gradient += points[i]
            if (i == y) break

            val from = ciePoints[i]
            val to = ciePoints[i + 1]

            val k = if (c-- > 0) b + 1 else b
            val m = k + 1
            val step = 1.0f / m

            val cieMid = FloatArray(3)

            for (j in 0 until k) {
                val t = (j + 1) * step
                cieMid[0] = from[0] + t * (to[0] - from[0])
                cieMid[1] = from[1] + t * (to[1] - from[1])
                cieMid[2] = from[2] + t * (to[2] - from[2])

                val rgb = sRGB.fromCIEXYZ(cieMid)
                gradient += TextColor.color(rgb[0], rgb[1], rgb[2])
            }
        }

        return gradient
    }
}

val TextColor.invert get() = TextColor.color(value() xor 0x00FFFFFF)

fun TextColor.toShadowColor(alpha: Double = 1.0): ShadowColor {
    return ShadowColor.shadowColor(this, (alpha * 255).toInt())
}

val String.color: TextColor get() = TextColor.fromHexString(this) ?: ColorUtil.WHITE
val String.shadow: ShadowColor get() = ShadowColor.fromHexString(this) ?: ColorUtil.WHITE.toShadowColor()

val Int.color: TextColor get() = TextColor.color(this)
val Int.shadow: ShadowColor get() = ShadowColor.shadowColor(this)

fun String.shadow(alpha: Double): ShadowColor {
    return TextColor.fromHexString(this)?.toShadowColor(alpha) ?: ColorUtil.WHITE.toShadowColor()
}

fun Int.shadow(alpha: Double): ShadowColor {
    return TextColor.color(this).toShadowColor(alpha)
}

fun TextColor.toBukkitColor(): org.bukkit.Color {
    return org.bukkit.Color.fromRGB(value())
}