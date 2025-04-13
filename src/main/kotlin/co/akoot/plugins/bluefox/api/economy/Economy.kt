package co.akoot.plugins.bluefox.api.economy

object Economy {
    object Error {
        const val INSUFFICIENT_BALANCE = -1
        const val INSUFFICIENT_SELLER_BALANCE = -2
        const val INSUFFICIENT_BUYER_BALANCE = -3
        const val MISSING_COIN = -4
        const val MISSING_SELLER_COIN = -5
        const val MISSING_BUYER_COIN = -6
        const val SQL_ERROR = -7
    }
}