package co.akoot.plugins.bluefox.api.economy

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_BALANCE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.MISSING_COIN
import co.akoot.plugins.bluefox.api.economy.Economy.Error.SQL_ERROR
import co.akoot.plugins.bluefox.api.economy.Economy.Success.SUCCESS
import co.akoot.plugins.bluefox.extensions.defaultWalletAddress
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.sql.Statement

open class Wallet(val id: Int, val address: String) {

    companion object {
        var WORLD = WorldWallet
        var BANK = Wallet(2, "BANK")

        fun get(address: String): Wallet? {
            val statement = BlueFox.db.prepareStatement("SELECT id FROM wallets WHERE address = ?")
            statement.setString(1, address)
            val resultSet = runCatching { statement.executeQuery() }.getOrNull() ?: return null
            while(resultSet.next()) {
                val id = resultSet.getInt("id")
                return Wallet(id, address)
            }
            return null
        }

        fun get(offlinePlayer: OfflinePlayer): Wallet? {
            return get(offlinePlayer.defaultWalletAddress)
        }
    }

    fun register(): Int {
        val statement = BlueFox.db.prepareStatement("INSERT INTO wallets VALUES (?, ?)")
        statement.run {
            setInt(1, id)
            setString(2, address)
        }
        val rows = runCatching { statement.executeUpdate() }.getOrElse { 0 }
        if(rows <= 0 ) return SQL_ERROR
        return SUCCESS
    }

    val balance: MutableMap<Coin, Double> = mutableMapOf()

    fun withdraw(player: Player, coin: Coin, amount: Int): Boolean {
        val balance = balance[coin] ?: return false
        if(balance <= amount) return false
        player.inventory.addItem(ItemStack(coin.backing, amount))
        return true
    }

    fun deposit(player: Player, coin: Coin, amount: Int): Boolean {
        if(!player.inventory.contains(coin.backing, amount)) return false
        player.inventory.removeItemAnySlot(ItemStack(coin.backing, amount))
        return true
    }

    open fun send(wallet: Wallet, coin: Coin, amount: Double, relatedId: Int? = null): Int {
        val currentBalance = balance[coin] ?: return MISSING_COIN
        if(currentBalance < amount) return INSUFFICIENT_BALANCE
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
        if(hasRelatedId) statement.setInt(5, relatedId!!)
        val rows = runCatching { statement.executeUpdate() }.getOrElse { 0 }
        if(rows <= 0 ) return SQL_ERROR
        val keys = statement.generatedKeys
        val success = keys.next()
        if(success) {
            balance[coin] = currentBalance - amount
            val recipientBalance = wallet.balance[coin] ?: 0.0
            wallet.balance[coin] = recipientBalance + amount
        }
        return runCatching { keys.getInt("id") }.getOrElse { SQL_ERROR }
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