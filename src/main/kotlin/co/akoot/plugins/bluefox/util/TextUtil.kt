package co.akoot.plugins.bluefox.util

import co.akoot.plugins.bluefox.BlueFox
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.awt.Color

class TextUtil {
    companion object {

        // Constants
        val EMPTY = Component.text("")

        // Misc regex
        val URL_REGEX = Regex("(?:https?://|www\\.)\\S+") // Very simple regex

        // Color regex
        val HEX_REGEX = Regex("#[a-f0-9]{6}", RegexOption.IGNORE_CASE)
        val LEGACY_CODE_REGEX = Regex("[a-f0-9xrh]", RegexOption.IGNORE_CASE)
        val PALETTE_COLOR_REGEX = Regex("\\{\\w+}")
        val COLOR_REGEX = Regex(
            "&((?:$PALETTE_COLOR_REGEX|$LEGACY_CODE_REGEX|$HEX_REGEX)(?:\\+(?:$PALETTE_COLOR_REGEX|$LEGACY_CODE_REGEX|$HEX_REGEX))*)([^&]+)",
            RegexOption.IGNORE_CASE
        )

        // Markdown regex
        val BOLD_REGEX = Regex("\\*\\*([^*]+)\\*\\*")
        val ITALIC_REGEX = Regex("\\*([^*]+)\\*")
        val BOLD_ITALIC_REGEX = Regex("\\*\\*\\*([^*]+)\\*\\*\\*")
        val UNDERLINE_REGEX = Regex("__([^_]+)__")
        val STRIKETHROUGH_REGEX = Regex("~~([^~]+)~~")
        val OBFUSCATED_REGEX = Regex("\\|\\|([^|]+)\\|\\|")

        // Clickable text regex
        val CLICKABLE_TEXT_REGEX = Regex("\\[([^]]+)]\\((?:\"([^)]+)\" |)([^)]+)\\)")

        // Hashified Regex
        val PING_REGEX = Regex("@(\\w+)")


        /**
         * Parse text using color and text formatting codes
         */
//        fun hashify(sender: User? = BlueFox.instance.console, message: String): String {
//            var text = message
//
//            // Green text
//            if (text[0] == '>') {
//                text = "&a$text"
//            }
//
//            // URLS
//            for (result in URL_REGEX.findAll(text)) {
//                val match = result.groups[0] ?: continue
//                text = text.replace(match.value, "[__*&{url}${match}*__]($match)")
//            }
//
//            // Pings
//            for (result in PING_REGEX.findAll(text)) {
//                val match = result.groups[0] ?: continue
//                val searchString = result.groupValues[1]
//                val selector = searchString.singleOrNull()
//                var replacement = text
//
//                // Players
//                if (selector == null) {
//                    val user = BlueFox.instance.getUser(searchString) ?: continue
//                    replacement =
//                        "[&(player)@$searchString](\"${user.username}\n${user.uuid}\" !/msg ${user.username} )"
//                }
//
//                // Selectors
//                when (selector) {
//                    'a' -> replacement =
//                        Bukkit.getOnlinePlayers().joinToString { it.name } //TODO: Replace with getUserList()
//                    'r' -> replacement = Bukkit.getOnlinePlayers().randomOrNull()?.name ?: ""
//                    's' -> replacement = sender?.username ?: "CONSOLE"
//                }
//
//                text = text.replace(match.value, replacement)
//            }
//
//            //TODO: Emojis, Stickers, Channels
//
//            return text
//        }

        /**
         * Converts a string to a text component
         */
        fun cowify(string: String): TextComponent {
            return Component.text(string)
        }

        /**
         * @return A colored TextComponent using the specified colors
         */
        fun color(text: String, vararg colors: TextColor): TextComponent {
            if (colors.isEmpty()) return Component.text(text)

            return if (colors.size == 1) {
                val color = colors[0]
                Component.text(text).color(color)
            } else {
                val gradient = ColorUtil.getGradient(text.length - text.count { it == ' ' }, *colors)
                val component = Component.text()
                var j = 0

                for (c in text) {
                    component.append(
                        if (c != ' ') Component.text(c).color(gradient[j++]) else Component.text(" ")
                    )
                }

                component.build()
            }
        }

        data class RangedComponent(val component: Component, val range: IntRange)

        fun parseMarkdown(
            text: String,
            pattern: Regex,
            rangedComponents: MutableList<RangedComponent>,
            vararg textDecoration: TextDecoration
        ) {
            for (underlineResult in pattern.findAll(text)) {
                val match = underlineResult.groups[0] ?: continue
                val textValue = underlineResult.groupValues[1]
                if (textValue.isBlank()) continue
                val component = parse(textValue).toBuilder()
                for (decoration in textDecoration) component.decorate(decoration)
                rangedComponents += RangedComponent(component.build(), match.range)
            }
        }

        /**
         * Parses plaintext into a TextComponent
         * @param text The text to parse
         * @return A TextComponent
         */
        fun parse(text: String): TextComponent {

            // Create a "final" component builder
            val finalComponent = Component.text()

            // Create a list of RangedComponents that stores each component as well as their ranges
            // in the original string
            val rangedComponents = mutableListOf<RangedComponent>()

            // Parse all clickable text
            for (result in CLICKABLE_TEXT_REGEX.findAll(text)) {

                // For whatever reason, skip this "result" if it doesn't have a group 0 (full regex match)
                // This shouldn't be possible
                val match = result.groups[0] ?: continue
                val title = Component.text(result.groupValues[1]).toBuilder()
                val hoverText = (result.groups[2] ?: result.groups[3])?.value ?: ""
                val content = result.groupValues[3]

                // Add hover text if it has any
                if (hoverText.isNotEmpty()) title.hoverEvent(HoverEvent.showText(parse(hoverText)))

                // If the content is a URL
                if (content.matches(URL_REGEX)) {
                    title.clickEvent(ClickEvent.openUrl(content))
                }

                // If the content is a command
                else if (content[0] == '/') {
                    title.clickEvent(ClickEvent.runCommand(content))
                }

                // If the content is a command suggestion
                else if (content.startsWith("!/")) {
                    title.clickEvent(ClickEvent.suggestCommand(content))
                }

                // If the content is to be copied to the clipboard
                else if (content[0] == '+') {
                    title.clickEvent(ClickEvent.copyToClipboard(content))
                }

                // If the content is a file on the player's computer
                // (this won't work probably ever, but it'd be funny if it did)
                else if (content[0] == '>') {
                    title.clickEvent(ClickEvent.openFile(content))
                }

                // Add to the list of ranged components
                rangedComponents += RangedComponent(title.build(), match.range)
            }

            // Parse markdown
            parseMarkdown(text, BOLD_ITALIC_REGEX, rangedComponents, TextDecoration.BOLD, TextDecoration.ITALIC)
            parseMarkdown(text, BOLD_REGEX, rangedComponents, TextDecoration.BOLD)
            parseMarkdown(text, ITALIC_REGEX, rangedComponents, TextDecoration.ITALIC)
            parseMarkdown(text, UNDERLINE_REGEX, rangedComponents, TextDecoration.UNDERLINED)
            parseMarkdown(text, STRIKETHROUGH_REGEX, rangedComponents, TextDecoration.STRIKETHROUGH)
            parseMarkdown(text, OBFUSCATED_REGEX, rangedComponents, TextDecoration.OBFUSCATED)

            // Parse color
            for (result in COLOR_REGEX.findAll(text)) {
                val match = result.groups[0] ?: continue
                val values = result.groupValues
                val colors = values[1].split("+").map { ColorUtil.getColor(it) }.toTypedArray()
                val content = values[2].trimStart()
                val component = color(content, *colors)
                rangedComponents += RangedComponent(component, match.range)
            }

            // Finally, combine all our scraps
            var lastIndex = 0
            for (component in rangedComponents.sortedBy { it.range.first }) {
                try {
                    finalComponent.append(Component.text(text.substring(lastIndex, component.range.first)))
                } catch (e: Exception) {
                    continue
                }
                finalComponent.append(component.component)
                lastIndex = component.range.last + 1
            }
            finalComponent.append(Component.text(text.substring(lastIndex)))

            return finalComponent.build()
        }
    }
}