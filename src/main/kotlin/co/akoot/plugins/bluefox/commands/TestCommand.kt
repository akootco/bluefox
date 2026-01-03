package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.api.CatCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.util.sendError
import co.akoot.plugins.bluefox.util.sendText
import co.akoot.plugins.bluefox.util.sendWarning
import com.mojang.brigadier.Command

class TestCommand(plugin: FoxPlugin): CatCommand(plugin,"crest") {
    init {
        then {
            float("amount", -1f, 2f) {
                val sender = getSender(it)
                val amount = getFloat(it, "amount")
                sender.sendText("very well ", amount)
                true
            } then {
                boolean {
                    val sender = getSender(it)
                    val amount = getFloat(it, "amount")
                    val trueOrFalse = getBoolean(it)
                    sender.sendWarning("now you did say ", amount, " and ", trueOrFalse, "?")
                    true
                }
            }
        }
        then {
            subcommand("b") {
                val sender = getSender(it)
                sender.sendMessage("you said b")
                true
            } then {
                player {
                    val player = getPlayer(it)
                    getSender(it).sendText("am gonna krill ", player, " and that's on molecule")
                    true
                }
            }
        }
        then {
            subcommand("c") {
                val sender = getSender(it)
                sender.sendError("you said c lol")
                true
            }
        }
    }
}