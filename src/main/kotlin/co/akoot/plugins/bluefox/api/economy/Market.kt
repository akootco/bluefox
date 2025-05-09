package co.akoot.plugins.bluefox.api.economy

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_BUYER_BALANCE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_SELLER_BALANCE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.MISSING_BUYER_COIN
import co.akoot.plugins.bluefox.api.economy.Economy.Error.MISSING_SELLER_COIN
import org.bukkit.Material

object Market {

    val coins: MutableMap<String, Coin> = mutableMapOf()
    val prices: MutableMap<Pair<Coin, Coin>, Double> = mutableMapOf()

    fun priceInDiamonds(coin: Coin): Double {
        val price = prices[coin to Coin.DIA] ?: -1.0
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

    fun trade(buyer: Wallet, seller: Wallet, buyerCoin: Coin, sellerCoin: Coin, buyerCoinAmount: Double, sellerCoinAmount: Double): Int {
        val sellerBalance = seller.balance[sellerCoin] ?: return MISSING_SELLER_COIN
        val buyerBalance = buyer.balance[buyerCoin] ?: return MISSING_BUYER_COIN
        if(sellerBalance < sellerCoinAmount) return INSUFFICIENT_SELLER_BALANCE
        if(buyerBalance < buyerCoinAmount) return INSUFFICIENT_BUYER_BALANCE
        val transactionId = buyer.send(seller, buyerCoin, buyerCoinAmount)
        val sentId =  seller.send(buyer, sellerCoin, sellerCoinAmount, transactionId)
        loadPrices()
        return sentId
    }

    fun getCoin(id: Int): Coin? {
        return coins.entries.find { it.value.id == id }?.value
    }

    fun load() {
        loadCoins()
        loadPrices()
        println("[market]")
        println(coins)
        println(prices)
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
        val result = statement.executeQuery()//runCatching { statement.executeQuery() }.getOrNull() ?: return
        while(result.next()) {
            try {
                val coin1 = getCoin(result.getInt("coin_id1")) ?: continue
                val coin2 = getCoin(result.getInt("coin_id2")) ?: continue
                prices[coin1 to coin2] = result.getDouble("price")
            } catch (_: Exception) {
                continue
            }
        }
    }

    fun loadCoins() {
        val statement = BlueFox.db.prepareStatement("SELECT * FROM coins")
        val result = statement.executeQuery()//runCatching { statement.executeQuery() }.getOrNull() ?: return
        while(result.next()) {
            try {
                val ticker = result.getString("ticker")
                val backing = if(ticker == "DIA") Material.DIAMOND
                else if(ticker == "NTRI") Material.NETHERITE_INGOT
                else Material.AIR
                val coin = Coin(
                    result.getInt("id"),
                    ticker,
                    result.getString("name"),
                    result.getString("description"),
                    backing
                )
                coins[ticker] = coin
            } catch (_: Exception) {
                continue
            }
        }
    }
}