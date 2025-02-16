package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.api.Profile
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import java.io.File
import java.util.*

fun OfflinePlayer.getDataFile(): File {
    return File("world/playerdata/$uniqueId.dat")
}

val OfflinePlayer.profile: Profile
    get() = Profile(this)