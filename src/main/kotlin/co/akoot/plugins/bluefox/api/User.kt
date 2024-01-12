package co.akoot.plugins.bluefox.api

import org.bukkit.OfflinePlayer
import java.util.*

class User(val uuid: UUID) {
    constructor(offlinePlayer: OfflinePlayer) : this(offlinePlayer.uniqueId)

    lateinit var username: String
}