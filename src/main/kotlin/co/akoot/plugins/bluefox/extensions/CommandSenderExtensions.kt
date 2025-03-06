package co.akoot.plugins.bluefox.extensions

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

val CommandSender.isBedrock: Boolean get() = this is Player && this.isBedrock