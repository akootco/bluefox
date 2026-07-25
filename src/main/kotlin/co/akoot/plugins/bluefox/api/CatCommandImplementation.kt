package co.akoot.plugins.bluefox.api

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack

class CatCommandImplementation(plugin: FoxPlugin, id: String, description: String = "A ${plugin.id} command.", vararg aliases: String, onCommand: CatCommand.(CommandContext<CommandSourceStack>) -> Boolean): CatCommand(plugin, id, description, *aliases) {
    init {
       executes { if(onCommand(it)) Command.SINGLE_SUCCESS else -1 }
    }
}