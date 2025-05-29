package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.api.economy.Coin

class CoinCreateEvent(val coin: Coin): FoxEventCancellable()