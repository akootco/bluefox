package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.extensions.mix
import co.akoot.plugins.bluefox.util.ColorUtil
import co.akoot.plugins.bluefox.util.Text

class CommandHelp(val text: Text = Text()) {

    constructor(string: String) : this(Text(string)) {}

    fun description(description: String): CommandHelp {
        text += Kolor.QUOTE(description).italic()
        return this
    }

    fun newLine(): CommandHelp {
        text += Text.newline
        return this
    }

    fun newLineSpace(): CommandHelp {
        text += Text("\n ")
        return this
    }

    fun usage(part: String, optional: Boolean = false, literal: Boolean = true, list: Boolean = false, final: Boolean = false): CommandHelp {
        val word = when {
            literal -> part
            optional -> "[$part]"
            list -> "[$part...]"
            else -> "<$part>"
        }
        text += Text(word, ColorUtil.randomColor())
        if(!final) text += Text.space
        return this
    }

    fun example(commandLine: String, description: String? = null): CommandHelp {
        val color = ColorUtil.randomColor()
        text += Text(commandLine, color.mix(Kolor.QUOTE))
        if(description != null) text += Text(" - $description", color)
        return this
    }
}