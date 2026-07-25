package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.BlueFox
import net.coreprotect.api.LookupOptions
import org.bukkit.block.Block
import kotlin.time.Duration
import kotlin.time.DurationUnit

fun Block.isNatural(since: Duration): Boolean? {
    val options = LookupOptions.builder()
        .time(since.toInt(DurationUnit.SECONDS))
        .build()
    val results = BlueFox.co?.blockLookup(this, options) ?: return null
    if(results.isEmpty()) return true
    val playerInteractions = results.filter { !it.player.startsWith("#") }
    return playerInteractions.isEmpty()
}