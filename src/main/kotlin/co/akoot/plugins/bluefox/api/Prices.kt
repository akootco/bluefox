package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.delegating.default
import co.akoot.plugins.bluefox.extensions.touch

object Prices {
    private val config = FoxConfig(BlueFox.instance.dataFolder.resolve("prices.conf").touch("{}"))
    object Spells {
        val tpa: Double by config default 10.0 from "spells"
        val weather: Double by config default 20.0 from "spells"
        val lightning: Double by config default 100.0 from "spells"
        val time: Double by config default 20.0 from "spells"
    }
    object Commands {
        val rtp: Double by config default 20.0 from "commands"
        val tp: Double by config default 100.0 from "commands"
        val tpa: Double by config default 10.0 from "commands"
        val edible: Double by config default 50.0 from "commands"
        val nick: Double by config default 250.0 from "commands"
        val setWarp: Double by config default 1000.0 from "commands"
        val setHome: Double by config default 10.0 from "commands"
        val back: Double by config default 10.0 from "commands"
    }
}