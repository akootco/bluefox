package co.akoot.plugins.bluefox.api.economy

import java.math.BigDecimal

object WorldWallet: Wallet(1, "WORLD") {
    override fun send(wallet: Wallet, coin: Coin, amount: BigDecimal, relatedId: Int?): Int {
        balance[coin] = amount // nice hacks
        return super.send(wallet, coin, amount, relatedId)
    }
}