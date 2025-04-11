package co.akoot.plugins.bluefox.api.economy.wallets

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.economy.Market

open class Wallet(val id: Int, val address: String) {

    val balance: MutableMap<String, Double> = mutableMapOf()

    fun deposit(ticker: String, amount: Double): Boolean {
        return Bank.send(this, ticker, amount)
    }

    fun depositDiamonds(amount: Int): Boolean {
        return Bank.send(this, "DIA", amount.toDouble())
    }

    fun depositNetherite(amount: Int): Boolean {
        return Bank.send(this, "NTRI", amount.toDouble())
    }

    fun send(wallet: Wallet, ticker: String, amount: Double): Boolean {
        val currentBalance = balance[ticker] ?: return false
        if(currentBalance < amount) return false
        val coin = Market.coins[ticker] ?: return false
        val statement = BlueFox.db.prepareStatement("""
            INSERT INTO wallet_transactions (coin_id,sender_id,recipient_id,amount) VALUES (?,?,?,?)
        """.trimIndent())
        statement.setInt(1, coin.id)
        statement.setInt(2, this.id)
        statement.setInt(3, wallet.id)
        statement.setDouble(4, amount)
        val success = runCatching { statement.executeUpdate() }.isSuccess
        if(success) {
            balance[ticker] = currentBalance - amount
            val recipientBalance = wallet.balance[ticker] ?: 0.0
            wallet.balance[ticker] = recipientBalance + amount
        }
        return success
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
            balance[coin.ticker] = resultSet.getDouble("balance")
        }
    }
}