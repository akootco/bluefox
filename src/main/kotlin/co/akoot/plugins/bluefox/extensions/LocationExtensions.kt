package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.api.XYZ
import co.akoot.plugins.bluefox.util.Color
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.bluefox.util.join
import co.akoot.plugins.bluefox.util.tertiary
import co.akoot.plugins.bluefox.util.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location

val Location.text: Text get() = Text() + blockX + ", " + blockY + ", " + blockZ + " in " + world.text()

val Location.xyz: XYZ get() = XYZ(this)

val Location.tpCommand: String get() = "tp $x $y $z"
val Location.tpCommandPrecise: String get() = "execute in ${world.executeName} run tp @s $x $y $z $yaw $pitch"
fun Location.component(color: TextColor = Color.Tertiary): Component = text(blockX, ", ", blockY, ", ", blockZ, " in ", world.component()).join("")
