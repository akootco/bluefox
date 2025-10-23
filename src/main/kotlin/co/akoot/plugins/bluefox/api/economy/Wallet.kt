package co.akoot.plugins.bluefox.api.economy

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.economy.Economy.Error.COIN_HAS_NO_BACKING
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_BALANCE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INSUFFICIENT_ITEMS
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INVALID_GAME_MODE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.INVALID_WORLD
import co.akoot.plugins.bluefox.api.economy.Economy.Error.NUMBER_TOO_SMALL
import co.akoot.plugins.bluefox.api.economy.Economy.Error.PRICE_UNAVAILABLE
import co.akoot.plugins.bluefox.api.economy.Economy.Error.SQL_ERROR
import co.akoot.plugins.bluefox.api.events.PlayerDepositEvent
import co.akoot.plugins.bluefox.api.events.PlayerWithdrawEvent
import co.akoot.plugins.bluefox.api.events.WalletSendCoinEvent
import co.akoot.plugins.bluefox.api.events.WalletRequestSwapEvent
import co.akoot.plugins.bluefox.extensions.defaultWalletAddress
import co.akoot.plugins.bluefox.extensions.giveInBlocks
import co.akoot.plugins.bluefox.extensions.isSurventure
import co.akoot.plugins.bluefox.extensions.removeIncludingBlocks
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Statement

open class Wallet(val id: Int, val address: String) {

    companion object {
        var WORLD = Wallet(1, "WORLD")
        var BANK = Wallet(2, "BANK")
        val playerWallets: MutableMap<OfflinePlayer, Wallet> = mutableMapOf()

        fun get(address: String): Wallet? {
            val statement = BlueFox.db.prepareStatement("SELECT id FROM wallets WHERE address = ?")
            statement.setString(1, address)
            val resultSet = statement.executeQuery()//runCatching { statement.executeQuery() }.getOrNull() ?: return null
            while(resultSet.next()) {
                val id = resultSet.getInt("id")
                return Wallet(id, address)
            }
            return null
        }

        fun get(offlinePlayer: OfflinePlayer): Wallet? {
            return get(offlinePlayer.defaultWalletAddress)
        }

        fun create(address: String): Wallet? {
            val existingWallet = get(address)
            if(existingWallet != null) return existingWallet
            val statement = BlueFox.db.prepareStatement("INSERT INTO wallets (address) VALUES (?)")
            statement.run {
                setString(1, address)
            }
            val rows = statement.executeUpdate()//runCatching { statement.executeUpdate() }.getOrElse { 0 }
            if(rows <= 0 ) return null
            return get(address)
        }

        fun create(offlinePlayer: OfflinePlayer): Wallet? {
            val wallet = create(offlinePlayer.defaultWalletAddress) ?: return null
            playerWallets[offlinePlayer] = wallet
            return wallet
        }
    }

    val balance: MutableMap<Coin, BigDecimal> = mutableMapOf()
    val hasUnlimitedMoney get() = this == WORLD || this == BANK
    val offlinePlayer: OfflinePlayer? get() = playerWallets.entries.find { it.value == this }?.key
    val player: Player? get() = offlinePlayer?.player

    fun withdraw(player: Player, coin: Coin, amount: Int): Int {
        if(!player.isOp) {
            if (!player.isSurventure) return INVALID_GAME_MODE // best not risk it
            if (player.world.name !in BlueFox.instance.settings.getStringList("wallet.worlds")) return INVALID_WORLD
        }
        if (coin.backing == null) return COIN_HAS_NO_BACKING
        val balance = balance[coin] ?: return INSUFFICIENT_BALANCE
        if (amount < 1) return NUMBER_TOO_SMALL
        if(balance < BigDecimal(amount)) return INSUFFICIENT_BALANCE
        PlayerWithdrawEvent(player, coin, amount).fire() ?: return Economy.Error.EVENT_CANCELLED
        if(coin.backingBlock == null) {
            var remaining = amount
            while (remaining > 0) {
                val stackSize = minOf(remaining, coin.backing.maxStackSize)
                val leftovers = player.inventory.addItem(ItemStack(coin.backing, stackSize))

                leftovers.values.forEach { player.dropItem(it) }
                remaining -= stackSize
            }
        } else {
            player.giveInBlocks(coin.backing, coin.backingBlock, amount)
        }
        return send(WORLD, coin, BigDecimal(amount))
    }

    fun deposit(player: Player, coin: Coin, amount: Int): Int {
        if(!player.isOp) {
            if (!player.isSurventure) return INVALID_GAME_MODE
            if (player.world.name !in BlueFox.instance.settings.getStringList("wallet.worlds")) return INVALID_WORLD
        }
        if (coin.backing == null) return COIN_HAS_NO_BACKING
        if (amount < 1) return INSUFFICIENT_ITEMS
        PlayerDepositEvent(player, coin, amount).fire() ?: return Economy.Error.EVENT_CANCELLED
        if(coin.backingBlock == null) {
            if (!player.inventory.contains(coin.backing, amount)) return INSUFFICIENT_ITEMS
            player.inventory.removeItemAnySlot(ItemStack(coin.backing, amount))
        } else {
            val result = player.removeIncludingBlocks(coin.backing, coin.backingBlock, amount)
            if (!result) return INSUFFICIENT_ITEMS
        }
        return WORLD.send(this, coin, BigDecimal(amount))
    }

    fun swap(coin1: Coin, coin2: Coin, amount: BigDecimal): Int {
        val price = Market.prices[coin2 to coin1] ?: return PRICE_UNAVAILABLE
        val amount2 = amount.multiply(price).setScale(8, RoundingMode.HALF_UP)
        WalletRequestSwapEvent(this, coin1, coin2, amount, amount2).fire() ?: return Economy.Error.EVENT_CANCELLED
        return Market.trade(this, WORLD, coin1, coin2, amount, amount2)
    }

    open fun send(wallet: Wallet, coin: Coin, amount: BigDecimal, relatedId: Int? = null): Int {
        val currentBalance = balance[coin] ?: BigDecimal.ZERO//return MISSING_COIN
        if(!hasUnlimitedMoney && currentBalance < amount) return INSUFFICIENT_BALANCE
        WalletSendCoinEvent(this, wallet, coin, amount, relatedId).fire() ?: return Economy.Error.EVENT_CANCELLED
        val hasRelatedId = relatedId != null
        val extraRelated = if(hasRelatedId) ",related_transaction" to ",?" else "" to ""
        val statement = BlueFox.db.prepareStatement("""
            INSERT INTO wallet_transactions (coin_id,sender_id,recipient_id,amount${extraRelated.first}) 
            VALUES (?,?,?,?${extraRelated.second})
        """.trimIndent(), Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, coin.id)
        statement.setInt(2, this.id)
        statement.setInt(3, wallet.id)
        statement.setBigDecimal(4, amount)
        if(hasRelatedId) statement.setInt(5, relatedId)
        val rows = runCatching { statement.executeUpdate() }.getOrElse { 0 }
        if(rows <= 0 ) return SQL_ERROR
        val keys = statement.generatedKeys
        val success = keys.next()
        if(success) {
            if(!hasUnlimitedMoney) {
                balance[coin] = currentBalance - amount
            }
            val recipientBalance = wallet.balance[coin] ?: BigDecimal.ZERO
            wallet.balance[coin] = recipientBalance + amount
        }
        return keys.getInt(1)//runCatching { keys.getInt("id") }.getOrElse { println(it); SQL_ERROR }
    }

    fun load() {
        val statement = BlueFox.db.prepareStatement("""
            SELECT coin_id, (
                COALESCE(SUM(CASE WHEN recipient_id = $id THEN amount ELSE 0 END), 0) -
                COALESCE(SUM(CASE WHEN sender_id = $id THEN amount ELSE 0 END), 0)
            ) AS balance
            FROM wallet_transactions
            GROUP BY coin_id;
        """.trimIndent())
        val resultSet = statement.executeQuery()//runCatching { statement.executeQuery() }.getOrNull() ?: return
        while(resultSet.next()) {
            val coin = Market.getCoin(resultSet.getInt("coin_id")) ?: continue
            balance[coin] = resultSet.getBigDecimal("balance")
        }
    }
}