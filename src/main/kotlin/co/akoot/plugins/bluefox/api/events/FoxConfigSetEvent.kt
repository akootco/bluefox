package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.api.FoxConfig

class FoxConfigSetEvent(val config: FoxConfig, key: String, newValue: Any?) : FoxEventCancellable()