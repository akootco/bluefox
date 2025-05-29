package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.api.economy.Coin
import co.akoot.plugins.bluefox.api.economy.Wallet
import java.math.BigDecimal

class WalletSendCoinEvent(val sender: Wallet, val receiver: Wallet, val coin: Coin, val amount: BigDecimal, val relatedId: Int?): FoxEventCancellable() {
}