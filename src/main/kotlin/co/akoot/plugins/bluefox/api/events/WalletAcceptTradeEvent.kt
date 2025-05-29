package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.api.economy.Coin
import co.akoot.plugins.bluefox.api.economy.Wallet
import java.math.BigDecimal

class WalletAcceptTradeEvent(val buyer: Wallet, val seller: Wallet, val buyerCoin: Coin, val sellerCoin: Coin, val buyerCoinAmount: BigDecimal, val sellerCoinAmount: BigDecimal): FoxEventCancellable()