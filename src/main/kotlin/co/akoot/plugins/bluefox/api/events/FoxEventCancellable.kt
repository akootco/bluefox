package co.akoot.plugins.bluefox.api.events

import org.bukkit.event.Cancellable

abstract class FoxEventCancellable: FoxEvent(), Cancellable {
    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }
}