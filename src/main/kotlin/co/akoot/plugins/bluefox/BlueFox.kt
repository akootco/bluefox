package co.akoot.plugins.bluefox

import co.akoot.plugins.bluefox.api.FoxPlugin

class BlueFox : FoxPlugin() {

    override fun register() {
        logger.info("Good day!")
    }

    override fun unregister() {
        // Plugin shutdown logic
    }
}