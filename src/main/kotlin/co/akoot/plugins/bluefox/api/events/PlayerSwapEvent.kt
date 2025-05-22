package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.api.economy.Coin
import org.bukkit.entity.Player

class PlayerSwapEvent(val player: Player, val amount: Double, val coin1: Coin, val coin2: Coin): FoxEventCancellable()