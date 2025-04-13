package co.akoot.plugins.bluefox.api.economy

open class Coin(val id: Int, val ticker: String, val name: String, val description: String) {
    companion object {
        var DIA = Coin(3, "DIA", "Diamond", "Diamond Coin")
        var NTRI = Coin(4, "NTRI", "Netherite", "Netherite Coin")
    }
}