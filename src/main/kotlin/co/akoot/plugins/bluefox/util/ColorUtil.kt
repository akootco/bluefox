package co.akoot.plugins.bluefox.util

import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import java.awt.Color
import java.awt.color.ColorSpace
import java.util.*
import kotlin.random.Random

object ColorUtil {

    val WHITE = TextColor.color(0xffffff)
    val TRANSPARENT = ShadowColor.shadowColor(0x000000)

    val color = mapOf(
        "text" to TextColor.color(0xfcf7f4),
        "accent" to TextColor.color(0x97d4fc),
        "player" to TextColor.color(0xfc97c9),
        "number" to TextColor.color(0xfce497),
        "error_text" to TextColor.color(0xfc5f5f),
        "error_accent" to TextColor.color(0xfc8888),
        "error_player" to TextColor.color(0xfc88c2),
        "error_number" to TextColor.color(0xfcae5f),
    )

    val colorBedrock = mapOf(
        "text" to TextColor.color(0xFFFFFF),
        "accent" to TextColor.color(0x55FFFF),
        "player" to TextColor.color(0xFF55FF),
        "number" to TextColor.color(0xFFFF55),
        "error_text" to TextColor.color(0xAA0000),
        "error_accent" to TextColor.color(0xFF5555),
        "error_player" to TextColor.color(0xFF5555),
        "error_number" to TextColor.color(0xFF5555),
    )

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

    val colorTinted = color.mapValues { (_, color) ->
        mix(color, MONTH_COLOR)
    }

    /**
     * Get the TextColor with the specified name
     *
     * @param name The name of the color
     * @param bedrock Whether the player viewing the color is bedrock
     * @return A TextColor object if it exists in the list of colors, WHITE otherwise
     */
    fun getColor(name: String, bedrock: Boolean = false): TextColor {
        return (if (bedrock) colorBedrock[name] else color[name]) ?: WHITE
    }

    /**
     * Get the TextColor with the specified name
     *
     * @param name The name of the color
     * @param bedrock Whether the player viewing the color is bedrock
     * @param alpha The alpha of the shadow color
     * @return A ShadowColor object if it exists in the list of colors, WHITE otherwise
     */
    fun getShadowColor(name: String, alpha: Double = 1.0, bedrock: Boolean = false): ShadowColor {
        return getColor(name, bedrock).toShadowColor(alpha)
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
    fun lighten(color: Int, percentage: Double = 0.1): TextColor {
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
    fun mix(color1: TextColor, color2: TextColor): TextColor {
        return getGradient(3, color1, color2)[1]
    }

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

        val gradient: MutableList<TextColor> = mutableListOf()

        val cie = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ)
        val sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB)

        // Don't you HATE the funny little letter variables below?

        val x = points.size // Number of points
        val y = x - 1       // Number of relations

        val a = size - x    // Total number of midpoints
        val b = a / y       // Minimum number of midpoints per relation
        var c = a - (b * y) // Total number of extra midpoints

        for (i in 0 until x) {

            val from = points[i]
            gradient += from

            if (i == y) break // The final color

            val to = points[i + 1]

            val cieFrom = cie.fromRGB(Color(from.value()).getRGBColorComponents(null))
            val cieTo = cie.fromRGB(Color(to.value()).getRGBColorComponents(null))

            val k = if (c-- > 0) b + 1 else b // Number of colors to generate for this relation
            val m = k + 1 // We only need to include the colors IN BETWEEN

            for (j in 0 until m) {
                val l = j + 1
                val rgb = sRGB.fromCIEXYZ(
                    floatArrayOf(
                        cieFrom[0] + (l * (1.0f / m)) * (cieTo[0] - cieFrom[0]),
                        cieFrom[1] + (l * (1.0f / m)) * (cieTo[1] - cieFrom[1]),
                        cieFrom[2] + (l * (1.0f / m)) * (cieTo[2] - cieFrom[2])
                    )
                )
                if (j < k) gradient += TextColor.color(rgb[0], rgb[1], rgb[2])
            }
        }

        return gradient
    }
}

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