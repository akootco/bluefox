package co.akoot.plugins.bluefox.api.economy.wallets

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.economy.Market
import co.akoot.plugins.bluefox.api.economy.coins.Coin
import java.sql.Statement

open class Wallet(val id: Int, val address: String) {

    val balance: MutableMap<Coin, Double> = mutableMapOf()

    fun deposit(coin: Coin, amount: Double): Int {
        return Bank.send(this, coin, amount)
    }

    fun depositDiamonds(amount: Int): Int {
        return Bank.send(this, Coin.DIA, amount.toDouble())
    }

    fun depositNetherite(amount: Int): Int {
        return Bank.send(this, Coin.NTRI, amount.toDouble())
    }

    fun send(wallet: Wallet, coin: Coin, amount: Double, relatedId: Int? = null): Int {
        val currentBalance = balance[coin] ?: return -1
        if(currentBalance < amount) return -1
        val hasRelatedId = relatedId != null
        val extraRelated = if(hasRelatedId) ",related_id" to ",?" else "" to ""
        val statement = BlueFox.db.prepareStatement("""
            INSERT INTO wallet_transactions (coin_id,sender_id,recipient_id,amount${extraRelated.first}) 
            VALUES (?,?,?,?${extraRelated.second})
        """.trimIndent(), Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, coin.id)
        statement.setInt(2, this.id)
        statement.setInt(3, wallet.id)
        statement.setDouble(4, amount)
        if(hasRelatedId) statement.setInt(6, relatedId!!)
        val rows = runCatching { statement.executeUpdate() }.getOrElse { -1 }
        if(rows <= 0 ) return -1
        val keys = statement.generatedKeys
        val success = keys.next()
        if(success) {
            balance[coin] = currentBalance - amount
            val recipientBalance = wallet.balance[coin] ?: 0.0
            wallet.balance[coin] = recipientBalance + amount
        }
        return runCatching { keys.getInt("id") }.getOrElse { -1 }
    }

    fun load() {
        val statement = BlueFox.db.prepareStatement("""
            SELECT coin_id, (
                COALESCE(SUM(CASE WHEN recipient_id = $id THEN amount ELSE 0 END), 0) -
                COALESCE(SUM(CASE WHEN sender_id = $id THEN amount ELSE 0 END), 0)
            ) AS balance
            FROM transactions
            GROUP BY coin_id;
        """.trimIndent())
        val resultSet = runCatching { statement.executeQuery() }.getOrNull() ?: return
        while(resultSet.next()) {
            val coin = Market.getCoin(resultSet.getInt("coin_id")) ?: continue
            balance[coin] = resultSet.getDouble("balance")
        }
    }
}