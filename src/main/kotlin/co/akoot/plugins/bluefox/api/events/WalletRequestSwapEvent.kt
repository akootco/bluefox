package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.api.economy.Coin
import co.akoot.plugins.bluefox.api.economy.Wallet
import java.math.BigDecimal

class WalletRequestSwapEvent(val wallet: Wallet, val coin1: Coin, val coin2: Coin, val amount1: BigDecimal, val amount2: BigDecimal): FoxEventCancellable()