package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.CatCommand
import co.akoot.plugins.bluefox.extensions.roles
import co.akoot.plugins.bluefox.extensions.settings
import co.akoot.plugins.bluefox.extensions.usernamePossessive
import co.akoot.plugins.bluefox.util.*
import com.typesafe.config.ConfigValueType
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SettingsCommand(plugin: BlueFox) : CatCommand(plugin, "settings") {

    private val keys = mapOf(
        "normal" to setOf(),
        "pro" to setOf(),
        "vip" to setOf(
            "tpSound",
        ),
        "investor" to setOf(),
        "stakeholder" to setOf(),
        "admin" to setOf(),
    )

    init {
        noargs {
            permissionCheck(it) ?: return@noargs false
            val player = getPlayerSender(it) ?: return@noargs false
            sendSettings(player, player)
        }
        then {
            string("key", suggestions = { ctx, builder ->
                val sender = getSender(ctx)
                getKeys(sender).forEach { key ->
                    builder.suggest(key)
                }
            }) {
                permissionCheck(it) ?: false
                val player = getPlayerSender(it) ?: return@string false
                val key = getString(it, "key")
                getSetting(player, key)?.let { setting ->  player.sendText(setting) } ?: player.sendWarning("Value unset!")
            } then {
                string("value") {
                    permissionCheck(it) ?: false
                    val player = getPlayerSender(it) ?: return@string false
                    val key = getString(it, "key")
                    val value = getString(it, "value")
                    setSetting(player, player, key, value)
                }
            }
        }
        then {
            offlinePlayer {
                permissionCheck(it, "player") ?: return@offlinePlayer false
                val sender = getSender(it)
                sender.sendWarning("Set what?")
            } then {
                string("key", suggestions = { ctx, builder ->
                    val sender = getSender(ctx)
                    getKeys(sender).forEach { key ->
                        builder.suggest(key)
                    }
                }) {
                    permissionCheck(it, "player") ?: return@string false
                    val sender = getSender(it)
                    val player = getOfflinePlayer(it) ?: return@string false
                    val key = getString(it, "key")
                    getSetting(player, key)?.let { setting -> sender.sendText(setting) } ?: sender.sendWarning("Value unset!")
                } then {
                    string("value") {
                        permissionCheck(it, "player") ?: return@string false
                        val sender = getSender(it)
                        val player = getOfflinePlayer(it) ?: return@string false
                        val key = getString(it, "key")
                        val value = getString(it, "value")
                        setSetting(sender, player, key, value)
                    }
                }
            }
        }
    }

    fun sendSettings(sender: CommandSender, player: OfflinePlayer): Boolean {
        val settings = getKeys(sender).map {
            text(it, " = ",  getSetting(player, it)).join("")
        }.toMutableList()
        sender.sendText(settings.join("\n"))
        return true
    }

    fun getSetting(player: OfflinePlayer, key: String): Component? {
        val configValue = player.settings.conf.root()[key] ?: return null
        val value = configValue.unwrapped() ?: return null
        val type = configValue.valueType() ?: return null
        return when (type) {
            ConfigValueType.BOOLEAN -> (value as Boolean).get(Color.Primary, Color.Secondary) + value.toString()
            ConfigValueType.OBJECT -> Color.Secondary + configValue.render()
            ConfigValueType.NUMBER -> Color.Number + value.toString()
            ConfigValueType.NULL -> Color.Error + "null"
            ConfigValueType.LIST -> text("[", player.settings.getStringList(key).joinToString(", "), "]").join("")
            else -> Color.Primary + value.toString()
        }
    }

    fun getKeys(sender: CommandSender): List<String> {
        val roles = if (sender is Player) {
            if(sender.isOp) keys.keys
            else sender.roles
        }
        else keys.keys
        val validKeys = mutableListOf<String>()
        for (role in roles) {
            keys[role]?.let { validKeys.addAll(it) } ?: continue
        }
        return validKeys
    }

    fun setSetting(sender: CommandSender, player: OfflinePlayer, key: String, value: Any?): Boolean {
        val target = (player == sender).get("", "${player.usernamePossessive} ")
        val validKeys = getKeys(sender)
        if (key !in validKeys) return sender.sendWarning("Can't change this one, sorry!")
        player.settings.set(key, value)
        return if (value == null) sender.sendText("Unset ", target, primary(key))
        else sender.sendText("Set ", target, primary(key), " to ", tertiary(value))
    }
}