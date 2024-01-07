package co.akoot.plugins.bluefox.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

class ChatUtil {
    companion object {

        /**
         * Parse text using color and text formatting codes
         */
        fun parse(string: String): TextComponent {
            return cowify(string)
        }

        /**
         * Converts a string to a text component
         */
        fun cowify(string: String): TextComponent {
            return Component.text(string)
        }
    }
}