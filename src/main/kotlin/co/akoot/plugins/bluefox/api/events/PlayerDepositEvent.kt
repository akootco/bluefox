package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.api.economy.Coin
import org.bukkit.entity.Player

class PlayerDepositEvent(val player: Player, val coin: Coin, val amount: Double): FoxEventCancellable()