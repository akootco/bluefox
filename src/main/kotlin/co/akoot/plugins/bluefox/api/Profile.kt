package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.economy.Wallet
import org.bukkit.OfflinePlayer
import java.util.*

class Profile(val uuid: UUID) {
    fun getId(): Int {
        val statement = BlueFox.db.prepareStatement("""
            SELECT id from user_id where uuid = ?;
        """.trimIndent())
        statement.setString(1, uuid.toString())
        val resultSet = statement.executeQuery()
        var id = -1
        while(resultSet.next()) {
            id = resultSet.getInt("id")
        }
        return id
    }
}