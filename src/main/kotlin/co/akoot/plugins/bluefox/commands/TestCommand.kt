package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.api.CatCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import com.mojang.brigadier.Command

class TestCommand(plugin: FoxPlugin): CatCommand(plugin,"test") {
    init {
        then(float("amount", -1f, 2f) {
                val sender = getSender(it)
                val amount = getFloat(it, "amount")
                sender.sendMessage("very well $amount")
                true
            }
            .then(boolean() {
                val sender = getSender(it)
                val amount = getFloat(it, "amount")
                val trueOrFalse = getBoolean(it)
                sender.sendMessage("now you did say $amount and $trueOrFalse")
                true
            })
        )
        then(subcommand("b").then(player() {
            val player = getPlayer(it)
            getSender(it).sendMessage("am gonna krill ${player.name}")
            true
        }).executes {
            val sender = getSender(it)
            sender.sendMessage("you said b")
            win
        })
        then(subcommand("c") {
            val sender = getSender(it)
            sender.sendMessage("you said c lol")
            true
        })
    }
}