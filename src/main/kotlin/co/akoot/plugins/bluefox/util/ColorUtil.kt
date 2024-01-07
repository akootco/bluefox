package co.akoot.plugins.bluefox.util

import java.awt.Color
import java.awt.color.ColorSpace
import kotlin.random.Random

class ColorUtil {
    companion object {

        /**
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
         * @return A hex string representation of the color
         */
        fun getHexString(color: Color, lowercase: Boolean = true): String {
            return String.format("#%02x%02x%02x", color.red, color.green, color.blue)
                .let { if (lowercase) it.lowercase() else it.uppercase() }
        }

        /**
         * @return A lighter color
         */
        fun lighten(color: Int, percentage: Double = 0.1): Int {
            val col = Color(color)
            val min = (255 * percentage).toInt()
            return Color(
                (col.red + (col.red * percentage)).toInt().coerceAtMost(255).coerceAtLeast(min),
                (col.green + (col.green * percentage)).toInt().coerceAtMost(255).coerceAtLeast(min),
                (col.blue + (col.blue * percentage)).toInt().coerceAtMost(255).coerceAtLeast(min)
            ).rgb
        }

        /**
         * @return A darker color
         */
        fun darken(color: Int, percentage: Double = 0.1): Int {
            val col = Color(color).darker()
            val factor = 1 - percentage
            return Color(
                (col.red * factor).toInt().coerceAtLeast(0),
                (col.green * factor).toInt().coerceAtLeast(0),
                (col.blue * factor).toInt().coerceAtLeast(0)
            ).rgb
        }

        /**
         * @return A random color
         */
        fun randomColor(saturation: Float = 0.9f, brightness: Float = 0.9f): Color {
            return Color.getHSBColor(Random.nextFloat(), saturation, brightness)
        }

        /**
         * @return A random color in the form of a hex string
         */
        fun randomColorHex(saturation: Float = 0.9f, brightness: Float = 0.9f): String {
            return getHexString(randomColor(saturation, brightness))
        }

        /**
         * @return The resulting color of mixing color1 and color2
         */
        fun mix(color1: Int, color2: Int): Int {
            return getGradient(3, color1, color2)[1]
        }

        /**
         * Generates a gradient based on the color points provided.
         * This method mixes the colors in the CIE color space, which
         * results in more vibrant colors
         * @param size The number of colors to generate
         * @param points The colors to mix
         * @return A list of colors
         */
        fun getGradient(size: Int, vararg points: Int): MutableList<Int> {

            val gradient: MutableList<Int> = mutableListOf()

            val cie = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ)
            val sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB)

            // Don't you HATE the funny little letter variables below?

            val x = points.size // Number of points
            val y = x - 1       // Number of relations

            val a = size - x       // Total number of midpoints
            val b = a / y       // Minimum number of midpoints per relation
            var c = a - (b * y) // Total number of extra midpoints

            for (i in 0 until x) {

                val from = points[i]
                gradient += from

                if (i == y) break // The final color

                val to = points[i + 1]

                val cieFrom = cie.fromRGB(Color(from).getRGBColorComponents(null))
                val cieTo = cie.fromRGB(Color(to).getRGBColorComponents(null))

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
                    if (j < k) gradient += Color(rgb[0], rgb[1], rgb[2]).rgb
                }
            }

            return gradient
        }
    }
}