package co.akoot.plugins.bluefox.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

abstract class FoxEvent: Event() {
    companion object {
        private val handlers = HandlerList()
        @JvmStatic
        fun getHandlerList(): HandlerList = handlers
    }

    override fun getHandlers(): HandlerList = handlers
}