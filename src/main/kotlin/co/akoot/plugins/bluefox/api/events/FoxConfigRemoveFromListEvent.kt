package co.akoot.plugins.bluefox.api.events

import co.akoot.plugins.bluefox.api.FoxConfig

class FoxConfigRemoveFromListEvent(val config: FoxConfig, key: String, value: Any?) : FoxEventCancellable()