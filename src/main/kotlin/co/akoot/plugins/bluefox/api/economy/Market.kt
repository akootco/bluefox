package co.akoot.plugins.bluefox.api.economy

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.economy.coins.Coin
import co.akoot.plugins.bluefox.api.economy.coins.DiamondCoin
import co.akoot.plugins.bluefox.api.economy.wallets.Wallet

object Market {

    val coins: MutableMap<String, Coin> = mutableMapOf()
    val prices: MutableMap<Pair<String, String>, Double> = mutableMapOf()

    fun priceInDiamonds(coin: Coin): Double {
        val price = prices[coin.ticker to DiamondCoin.ticker] ?: -1.0
        return price
    }

    fun registerCoin(coin: Coin): Boolean {
        val statement = BlueFox.db.prepareStatement("INSERT INTO coins (ticker, name, description) VALUES (?,?,?)")
        statement.setString(1, coin.ticker)
        statement.setString(2, coin.name)
        statement.setString(3, coin.description)
        val success = runCatching { statement.executeUpdate() }.isSuccess
        if(success) coins[coin.ticker] = coin
        return success
    }

    fun trade(seller: Wallet, buyer: Wallet, coin1: Coin, coin2: Coin, price1: Double, price2: Double): Boolean {
        //todo: im houngry
        return true
    }

    fun getCoin(id: Int): Coin? {
        return coins.entries.find { it.value.id == id }?.value
    }

    fun load() {
        loadCoins()
        loadPrices()
    }

    fun loadPrices() {
        val statement = BlueFox.db.prepareStatement("""
            SELECT
                t1.coin_id AS coin_id1,
                t2.coin_id AS coin_id2,
                AVG(t1.amount * 1.0 / t2.amount) AS price
            FROM wallet_transactions t1
            JOIN wallet_transactions t2 ON t1.id = t2.related_transaction
            WHERE t2.related_transaction IS NOT NULL
            GROUP BY t1.coin_id, t2.coin_id;
        """.trimIndent())
        val result = runCatching { statement.executeQuery() }.getOrNull() ?: return
        while(result.next()) {
            try {
                val coin1 = getCoin(result.getInt("coin_id1")) ?: continue
                val coin2 = getCoin(result.getInt("coin_id2")) ?: continue
                prices[coin1.ticker to coin2.ticker] = result.getDouble("price")
            } catch (_: Exception) {
                continue
            }
        }
    }

    fun loadCoins() {
        val statement = BlueFox.db.prepareStatement("SELECT * FROM coins")
        val result = runCatching { statement.executeQuery() }.getOrNull() ?: return
        while(result.next()) {
            try {
                val ticker = result.getString("ticker")
                coins[ticker] = Coin(
                    result.getInt("id"),
                    ticker,
                    result.getString("name"),
                    result.getString("description")
                )
            } catch (_: Exception) {
                continue
            }
        }
    }
}