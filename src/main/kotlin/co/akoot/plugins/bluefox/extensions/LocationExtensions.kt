package co.akoot.plugins.bluefox.extensions

import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.util.Text
import net.kyori.adventure.text.Component
import org.bukkit.Location

val Location.text: Text get() = Text() + blockX + ", " + blockY + ", " + blockZ + " in " + Kolor.ACCENT(world.name)