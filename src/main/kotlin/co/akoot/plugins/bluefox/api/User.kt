package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.util.DbConfig
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.io.File
import java.util.*

class User(val uuid: UUID) {

    // Constants
    val userDir = File("users/$uuid")
    val offlinePlayer: OfflinePlayer = Bukkit.getOfflinePlayer(uuid)
    val player: Player? = offlinePlayer.player

    // Config sources
    val settings: FoxConfig = FoxConfig(userDir.resolve("settings.conf"))
    val profile: DbConfig = DbConfig(BlueFox.usersDb, "profiles", "uuid", uuid.toString())
    val statistics: DbConfig = DbConfig(BlueFox.usersDb, "statistics", "user_id", uuid.toString())

    // Variables
    val hasJoined: Boolean = offlinePlayer.hasPlayedBefore()
}