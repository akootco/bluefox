package co.akoot.plugins.bluefox.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor

class ChatUtil {
    companion object {

        val HEX_REGEX = Regex("#[a-f0-9]{6}", RegexOption.IGNORE_CASE)
        val LEGACY_CODE_REGEX = Regex("[a-f0-9xrh]", RegexOption.IGNORE_CASE)
        val PALETTE_COLOR_REGEX = Regex("\\{\\w+}")
        val COLOR_REGEX = Regex(
            "&((?:$PALETTE_COLOR_REGEX|$LEGACY_CODE_REGEX|$HEX_REGEX)(?:\\+(?:$PALETTE_COLOR_REGEX|$LEGACY_CODE_REGEX|$HEX_REGEX))*)([^&]+)",
            RegexOption.IGNORE_CASE
        )
        val BOLD_REGEX = Regex("\\*\\*([^*]+)\\*\\*")
        val BOLD_ITALIC_REGEX = Regex("\\*\\*\\*([^*]+)\\*\\*\\*")
        val UNDERLINE_REGEX = Regex("__([^_]+)__")
        val STRIKETHROUGH_REGEX = Regex("~~([^~]+)~~")
        val MAGIC_REGEX = Regex("\\|\\|([^|]+)\\|\\|")
        val CLICKABLE_TEXT = Regex("\\[[^]]+]\\([^)]+\\)")

        /**
         * Parse text using color and text formatting codes
         */
        fun parse(string: String): TextComponent {
            //TODO: Clickable text, formatted text, color text
            return cowify(string)
        }

        /**
         * Converts a string to a text component
         */
        fun cowify(string: String): TextComponent {
            return Component.text(string)
        }

        /**
         * @return A colored TextComponent using the specified colors
         */
        fun color(text: String, vararg colors: Int): TextComponent {
            if (colors.isEmpty()) return Component.text(text)

            return if (colors.size == 1) {
                val color = colors[0]
                Component.text(text).color(if (color == -1) null else TextColor.color(color))
            } else {
                val gradient = ColorUtil.getGradient(text.length - text.count { it == ' ' }, *colors)
                val component = Component.text()
                var j = 0

                for (c in text) {
                    component.append(
                        if (c != ' ') Component.text(c).color(TextColor.color(gradient[j++])) else Component.text(" ")
                    )
                }

                component.build()
            }
        }

        fun hashify() {

        }
    }
}