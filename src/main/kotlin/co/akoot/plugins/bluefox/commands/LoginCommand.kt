package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.CatCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.extensions.profile
import co.akoot.plugins.bluefox.extensions.sendMessage
import co.akoot.plugins.bluefox.util.Text.Companion.plus
import co.akoot.plugins.bluefox.util.copy
import co.akoot.plugins.bluefox.util.primary
import co.akoot.plugins.bluefox.util.quote
import co.akoot.plugins.bluefox.util.secondary
import co.akoot.plugins.bluefox.util.sendError
import co.akoot.plugins.bluefox.util.sendText
import co.akoot.plugins.bluefox.util.sendWarning

class LoginCommand(plugin: FoxPlugin): CatCommand(plugin, "login") {
    init {
        noargs {
            val player = getPlayerSender(it) ?: return@noargs false
            val token = player.profile.setToken()
            if (token == null) {
                player.sendError("Token system is currently down :( this sucks...")
                return@noargs false
            }
            player.sendText("Click ", secondary("here"), " to login to ", (primary(token) + quote(" akoot.co")).clickEvent(copy(token)))
//            player.sendWarning("DO NOT SHARE IT WITH ANYONE")
//            player.sendWarning(quote("only use it on akoot.co and akoot.cloud"))
            return@noargs true
        }
    }
}