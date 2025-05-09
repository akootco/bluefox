package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.api.Profile
import co.akoot.plugins.bluefox.api.economy.Wallet
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import org.geysermc.api.Geyser
import java.io.File
import java.util.*

val OfflinePlayer.defaultWalletAddress: String get() = this.uniqueId.toString().replace("-", "")

fun OfflinePlayer.getDataFile(): File {
    return File("world/playerdata/$uniqueId.dat")
}

val OfflinePlayer.profile: Profile
    get() = Profile(this)

fun List<OfflinePlayer>.names(): List<String> {
    return this.mapNotNull { it.name }
}

val Player.isBedrock: Boolean get() = Geyser.api().isBedrockPlayer(uniqueId)

val OfflinePlayer.wallet: Wallet? get() = Wallet.playerWallets[this]