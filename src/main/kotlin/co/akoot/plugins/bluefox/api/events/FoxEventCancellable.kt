package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.BlueFox
import org.bukkit.event.Cancellable

abstract class FoxEventCancellable: FoxEvent(), Cancellable {
    private var isCancelled = false
    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }

    fun fire(): Boolean? {
        BlueFox.server.pluginManager.callEvent(this)
        return if(isCancelled) null else true
    }
}