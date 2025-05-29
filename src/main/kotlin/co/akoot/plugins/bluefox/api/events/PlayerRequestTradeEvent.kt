package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.api.economy.Coin
import co.akoot.plugins.bluefox.api.economy.Wallet
import org.bukkit.entity.Player
import java.math.BigDecimal

class PlayerRequestTradeEvent(val player: Player, val target: Wallet, val coin: Coin, val amount: BigDecimal): FoxEventCancellable()