package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.api.economy.Coin
import co.akoot.plugins.bluefox.api.economy.Wallet
import org.bukkit.entity.Player
import java.math.BigDecimal

class PlayerRequestCoinEvent(val player: Player, val target: Wallet, val amount: BigDecimal, val coin: Coin): FoxEventCancellable()