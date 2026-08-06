package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.util.Color
import co.akoot.plugins.bluefox.util.Text.Companion.component
import net.coreprotect.api.LookupOptions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.OfflinePlayer
import org.bukkit.Tag
import org.bukkit.block.Block
import java.util.UUID
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

fun Block.component(color: TextColor = Color.Primary): Component {
    return Component.translatable(translationKey()).color(color)
}

var Block.owner: UUID?
    get() = chunk.getPDC<UUID>(BlueFox.key("$x.$y.$z.owner"))
    set(value) = chunk.setPDC(BlueFox.key("$x.$y.$z.owner"), value)

fun Block.clearOwner() = chunk.removePDC(BlueFox.key("$x.$y.$z.owner"))

fun Block.isOwner(player: OfflinePlayer): Boolean {
    return player.uniqueId == this.owner
}

val Block.isFreeRealEstate get() = owner == null

val Block.isSign: Boolean get() = Tag.ALL_SIGNS.isTagged(type)