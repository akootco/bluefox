package co.akoot.plugins.bluefox.api

import org.bukkit.Location
import java.util.UUID

data class Warp(val id: String, val name: String?, val description: String?, val author: UUID?, val location: Location, val visibility: Visibility?)