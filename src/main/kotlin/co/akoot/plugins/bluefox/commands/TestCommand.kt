package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.api.CatCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import com.mojang.brigadier.Command

class TestCommand(plugin: FoxPlugin): CatCommand(plugin,"test") {
    init {
        then(float("amount", -1f, 2f)
            .executes {
                val sender = getSender(it)
                val amount = getFloat(it, "amount")
                sender.sendMessage("very well $amount")
                Command.SINGLE_SUCCESS
            }
            .then(boolean().executes {
                val sender = getSender(it)
                val amount = getFloat(it, "amount")
                val trueOrFalse = getBoolean(it)
                sender.sendMessage("now you did say $amount and $trueOrFalse")
                Command.SINGLE_SUCCESS
            })
        )
        then(subcommand("b").then(player().executes {
            val player = getPlayer(it)
            getSender(it).sendMessage("am gonna krill ${player.name}")
            Command.SINGLE_SUCCESS
        }).executes {
            val sender = getSender(it)
            sender.sendMessage("you said b")
            Command.SINGLE_SUCCESS
        })
        then(subcommand("c").executes {
            val sender = getSender(it)
            sender.sendMessage("you said c lol")
            Command.SINGLE_SUCCESS
        })
    }
}