package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.BlueFox
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

abstract class FoxEvent: Event() {
    companion object {
        private val handlerList = HandlerList()
        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

    override fun getHandlers(): HandlerList = handlerList

    fun call() {
        BlueFox.server.pluginManager.callEvent(this)
    }
}