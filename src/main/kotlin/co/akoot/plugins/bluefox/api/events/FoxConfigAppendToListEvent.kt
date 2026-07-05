package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.api.FoxConfig

class FoxConfigAppendToListEvent(val config: FoxConfig, key: String, value: Any?) : FoxEventCancellable()