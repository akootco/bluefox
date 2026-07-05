package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.api.CatCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.extensions.profile
import co.akoot.plugins.bluefox.util.*
import co.akoot.plugins.bluefox.util.Text.Companion.plus

class TokenCommand(plugin: FoxPlugin) : CatCommand(plugin, "token") {
    init {
        noargs {
            val player = getPlayerSender(it) ?: return@noargs false
            val token = player.profile.setToken()
            if (token == null) {
                player.sendError("Token system is currently down :( this sucks...")
                return@noargs false
            }
            player.sendText(
                "Your ",
                secondary("token"),
                " is ",
                (primary(token) + quote(" (click to copy)")).clickEvent(copy(token))
            )
//            player.sendWarning("DO NOT SHARE IT WITH ANYONE")
//            player.sendWarning(quote("only use it on akoot.co and akoot.cloud"))
            return@noargs true
        }
    }
}