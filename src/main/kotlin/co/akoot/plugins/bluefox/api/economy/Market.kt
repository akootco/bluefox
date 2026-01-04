package co.akoot.plugins.bluefox.api.economy

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_BUYER_BALANCE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_SELLER_BALANCE
import co.akoot.plugins.bluefox.api.events.CoinCreateEvent
import co.akoot.plugins.bluefox.api.events.WalletAcceptTradeEvent
import co.akoot.plugins.bluefox.extensions.itemStack
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal
import java.math.RoundingMode

object Market {

    val coins: MutableMap<String, Coin> = mutableMapOf()
    val prices: MutableMap<Pair<Coin, Coin>, BigDecimal> = mutableMapOf()
    val pendingTrades: MutableMap<Pair<Wallet, Wallet>, Pair<Pair<Coin, BigDecimal>, Pair<Coin, BigDecimal>>> = mutableMapOf()

    fun BigDecimal.round(decimals: Int = 9): BigDecimal {
        return this.setScale(decimals, RoundingMode.HALF_UP)
    }

    fun getCoin(name: String): Coin? {
        return coins.entries.find { it.key.equals(name, true) }?.value
    }

    fun getTradeKey(parties: Pair<Wallet, Wallet>): Pair<Wallet, Wallet>? {
        val key1 = parties.first to parties.second
        val key2 = parties.second to parties.first
        return if(pendingTrades.containsKey(key1)) key1
        else if(pendingTrades.containsKey(key2)) key2
        else null
    }

    fun requestTrade(parties: Pair<Wallet, Wallet>, price1: Pair<Coin, BigDecimal>, price2: Pair<Coin, BigDecimal>): Boolean {
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

    fun priceInDiamonds(coin: Coin): BigDecimal {
        return prices[coin to Coin.DIA] ?: BigDecimal.ZERO
    }

    fun registerCoin(coin: Coin): Boolean {
        CoinCreateEvent(coin).fire() ?: return false
        val statement = BlueFox.db.prepareStatement("INSERT INTO coins (ticker, name, description) VALUES (?,?,?)")
        statement.setString(1, coin.ticker)
        statement.setString(2, coin.name)
        statement.setString(3, coin.description)
        val success = runCatching { statement.executeUpdate() }.isSuccess
        if(success) coins[coin.ticker] = coin
        return success
    }

    fun trade(buyer: Wallet, seller: Wallet, buyerCoin: Coin, sellerCoin: Coin, buyerCoinAmount: BigDecimal, sellerCoinAmount: BigDecimal): Int {
        val sellerBalance = seller.balance[sellerCoin] ?: BigDecimal.ZERO//return SELLER_MISSING_COIN
        val buyerBalance = buyer.balance[buyerCoin] ?: BigDecimal.ZERO//return BUYER_MISSING_COIN
        if(!seller.hasUnlimitedMoney && sellerBalance < sellerCoinAmount) return INSUFFICIENT_SELLER_BALANCE
        if(!buyer.hasUnlimitedMoney && buyerBalance < buyerCoinAmount) return INSUFFICIENT_BUYER_BALANCE
        WalletAcceptTradeEvent(buyer, seller, buyerCoin, sellerCoin, buyerCoinAmount, sellerCoinAmount).fire() ?: return Economy.Error.EVENT_CANCELLED
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
    }

    fun loadPrices() {
        val statement = BlueFox.db.prepareStatement("""
            SELECT
                t1.coin_id AS coin_id1,
                t2.coin_id AS coin_id2,
                AVG(t1.amount / t2.amount) AS price1
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
                val price1 = result.getBigDecimal("price1")
                prices.getOrPut(coin1 to coin2) { price1 }
                prices.getOrPut(coin2 to coin1) { BigDecimal.ONE.divide(price1, 16, RoundingMode.HALF_UP) }
            } catch (_: Exception) {
                continue
            }
        }
        // tsk tsk
        prices[Coin.NTRI to Coin.AD] = BigDecimal.ONE.divide(BigDecimal(4))
        prices[Coin.AD to Coin.NTRI] = BigDecimal(4)
        prices[Coin.DIA to Coin.AD] = prices[Coin.DIA to Coin.NTRI]!!.divide(BigDecimal(4))
        prices[Coin.AD to Coin.DIA] = prices[Coin.NTRI to Coin.DIA]!!.multiply(BigDecimal(4))
    }

    private data class CoinBacking(val backing: ItemStack? = null, val backingBlock: ItemStack? = null, val backingBlockAmount: Int = 9) {
        constructor(backing: Material, backingBlock: Material? = null, backingBlockAmount: Int = 9): this(backing.itemStack, backingBlock?.itemStack, backingBlockAmount)
    }

    fun loadCoins() {
        val statement = BlueFox.db.prepareStatement("SELECT * FROM coins")
        val result = statement.executeQuery()//runCatching { statement.executeQuery() }.getOrNull() ?: return
        while(result.next()) {
            try {
                val ticker = result.getString("ticker")
                val backing = when (ticker) {
                    "DIA" -> CoinBacking(Material.DIAMOND,  Material.DIAMOND_BLOCK)
                    "NTRI" -> CoinBacking(Material.NETHERITE_INGOT, Material.NETHERITE_BLOCK)
                    "AD" -> CoinBacking(Material.ANCIENT_DEBRIS)
                    "AMETHYST" -> CoinBacking(Coin.AMETHYST.backing, Coin.AMETHYST.backingBlock, 4)
                    else -> CoinBacking()
                }
                val coin = Coin(
                    result.getInt("id"),
                    ticker,
                    result.getString("name"),
                    result.getString("description"),
                    backing.backing,
                    backing.backingBlock,
                    backing.backingBlockAmount
                )
                coins[ticker] = coin
            } catch (_: Exception) {
                continue
            }
        }
        coins["AMETHYST"] = Coin.AMETHYST
    }
}