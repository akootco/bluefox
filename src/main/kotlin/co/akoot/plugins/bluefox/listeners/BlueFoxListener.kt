package co.akoot.plugins.bluefox.listeners

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.economy.Wallet
import co.akoot.plugins.bluefox.extensions.clearLoginMessages
import co.akoot.plugins.bluefox.extensions.deathMessage
import co.akoot.plugins.bluefox.extensions.loginMessages
import co.akoot.plugins.bluefox.util.Color
import co.akoot.plugins.bluefox.util.Text.Companion.asString
import co.akoot.plugins.bluefox.util.parse
import net.kyori.adventure.text.TextReplacementConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class BlueFoxListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        BlueFox.cachedOfflinePlayerNames += event.player.name
        val player = event.player
        val wallet = Wallet.get(player) ?: Wallet.create(player)
        if (wallet == null) {
            return
        }
        wallet.load()
        Wallet.playerWallets[player] = wallet

        player.loginMessages.forEach { message ->
            val component = message.parse(Color.Month)
            player.sendMessage(component)
        }
        player.clearLoginMessages()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (event.deathMessage()?.asString()?.endsWith(" died") != true) return
        val player = event.player
        player.deathMessage?.let {
            val message = it
                .replaceText(
                    TextReplacementConfig.builder()
                        .matchLiteral("%p").replacement(player.displayName()).build()
                )
                .replaceText(
                    TextReplacementConfig.builder()
                        .matchLiteral("%k").replacement(event.entity.displayName()).build()
                )
            event.deathMessage(message)
            player.deathMessage = null
        }
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        val player = event.player
        BlueFox.profiles.remove(player.uniqueId)
    }
}