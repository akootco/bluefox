package co.akoot.plugins.bluefox.api.economy

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_BUYER_BALANCE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_SELLER_BALANCE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.BUYER_MISSING_COIN
import co.akoot.plugins.bluefox.api.economy.Economy.Error.SELLER_MISSING_COIN
import org.bukkit.Material

object Market {

    val coins: MutableMap<String, Coin> = mutableMapOf()
    val prices: MutableMap<Pair<Coin, Coin>, Double> = mutableMapOf()
    val pendingTrades: MutableMap<Pair<Wallet, Wallet>, Pair<Pair<Coin, Double>, Pair<Coin, Double>>> = mutableMapOf()

    fun getTradeKey(parties: Pair<Wallet, Wallet>): Pair<Wallet, Wallet>? {
        val key1 = parties.first to parties.second
        val key2 = parties.second to parties.first
        return if(pendingTrades.containsKey(key1)) key1
        else if(pendingTrades.containsKey(key2)) key2
        else null
    }

    fun requestTrade(parties: Pair<Wallet, Wallet>, price1: Pair<Coin, Double>, price2: Pair<Coin, Double>): Boolean {
        val key = getTradeKey(parties)
        val value = price1 to price2
        return if(key != null) {
            pendingTrades[key] = value
            true
        } else {
            pendingTrades[parties.first to parties.second] = value
            false
        }
    }

    fun finalizeTrade(parties: Pair<Wallet, Wallet>): Boolean {
        loadPrices()
        return pendingTrades.remove(getTradeKey(parties)) != null
    }

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
        val sellerBalance = seller.balance[sellerCoin] ?: return SELLER_MISSING_COIN
        val buyerBalance = buyer.balance[buyerCoin] ?: return BUYER_MISSING_COIN
        if(sellerBalance < sellerCoinAmount) return INSUFFICIENT_SELLER_BALANCE
        if(buyerBalance < buyerCoinAmount) return INSUFFICIENT_BUYER_BALANCE
        val transactionId = buyer.send(seller, buyerCoin, buyerCoinAmount)
        val sentId =  seller.send(buyer, sellerCoin, sellerCoinAmount, transactionId)
        val statement = BlueFox.db.prepareStatement("UPDATE wallet_transactions SET related_transaction = ? WHERE id = ?")
        statement.setInt(1, sentId)
        statement.setInt(2, transactionId)
        statement.executeUpdate()
        finalizeTrade(buyer to seller)
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
                AVG(t1.amount / t2.amount) AS price1,
                AVG(t2.amount / t1.amount) AS price2
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
                val price1 = result.getDouble("price1")
                val price2 = result.getDouble("price2")
                prices[coin1 to coin2] = price1
                prices[coin2 to coin1] = price2
            } catch (_: Exception) {
                continue
            }
        }
        println("[prices]")
        println(prices)
    }

    fun loadCoins() {
        val statement = BlueFox.db.prepareStatement("SELECT * FROM coins")
        val result = statement.executeQuery()//runCatching { statement.executeQuery() }.getOrNull() ?: return
        while(result.next()) {
            try {
                val ticker = result.getString("ticker")
                val backing = when (ticker) {
                    "DIA" -> Material.DIAMOND to Material.DIAMOND_BLOCK
                    "NTRI" -> Material.NETHERITE_INGOT to Material.NETHERITE_BLOCK
                    "AD" -> Material.ANCIENT_DEBRIS to null
                    else -> null to null
                }
                val coin = Coin(
                    result.getInt("id"),
                    ticker,
                    result.getString("name"),
                    result.getString("description"),
                    backing.first,
                    backing.second
                )
                coins[ticker] = coin
            } catch (_: Exception) {
                continue
            }
        }
    }
}