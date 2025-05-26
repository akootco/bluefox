package co.akoot.plugins.bluefox.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

abstract class FoxEvent: Event() {
    companion object {
        private val handlerList = HandlerList()
        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

    override fun getHandlers(): HandlerList = handlerList
}