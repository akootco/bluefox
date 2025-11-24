package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.util.Text
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

val CommandSender.isBedrock: Boolean get() = this is Player && this.isBedrock

fun CommandSender.sendMessage(text: Text) = sendMessage(text.component)