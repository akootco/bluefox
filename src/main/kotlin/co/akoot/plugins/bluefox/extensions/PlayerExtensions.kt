package co.akoot.plugins.bluefox.extensions

import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import java.io.File

fun OfflinePlayer.getDataFile(): File {
    return File("world/playerdata/$uniqueId.dat")
}