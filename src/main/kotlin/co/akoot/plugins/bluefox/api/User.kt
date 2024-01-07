package co.akoot.plugins.bluefox.api

import org.bukkit.OfflinePlayer
import java.util.*

class User(uuid: UUID) {
    constructor(offlinePlayer: OfflinePlayer) : this(offlinePlayer.uniqueId)
}