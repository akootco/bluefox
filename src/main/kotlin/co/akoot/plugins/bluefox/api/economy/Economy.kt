package co.akoot.plugins.bluefox.api.economy

import org.bukkit.OfflinePlayer

object Economy {

    object Success {
        const val SUCCESS = 0
    }

    object Error {
        const val INSUFFICIENT_BALANCE = -1
        const val INSUFFICIENT_SELLER_BALANCE = -2
        const val INSUFFICIENT_BUYER_BALANCE = -3
        const val MISSING_COIN = -4
        const val MISSING_SELLER_COIN = -5
        const val MISSING_BUYER_COIN = -6
        const val SQL_ERROR = -7
        const val INSUFFICIENT_ITEMS = -8
        const val COIN_HAS_NO_BACKING = -9
        const val NUMBER_TOO_SMALL = -10
        const val PRICE_UNAVAILABLE = -11
    }
}