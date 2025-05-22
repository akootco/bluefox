package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.api.economy.Coin
import org.bukkit.entity.Player

class PlayerTradeEvent(val player1: Player, val player2: Player, val coin1: Coin, val coin2: Coin, val amount1: Double, val amount2: Double): FoxEventCancellable()