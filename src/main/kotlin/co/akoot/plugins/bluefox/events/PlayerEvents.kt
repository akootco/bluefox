package co.akoot.plugins.bluefox.events

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.economy.Wallet
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerEvents: Listener {
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
    }
}