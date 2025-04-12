package co.akoot.plugins.bluefox.api.economy.coins

import co.akoot.plugins.bluefox.api.economy.Market

open class Coin(val id: Int, val ticker: String, val name: String, val description: String) {
    companion object {
        val DIA get() = Market.coins["DIA"]!!
        val NTRI get() = Market.coins["NTRI"]!!
    }
}