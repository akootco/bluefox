package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.economy.Wallet
import org.bukkit.OfflinePlayer
import java.sql.Date
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.TemporalUnit
import java.util.*

class Profile(val uuid: UUID) {

    var id: Int? = getId() ?: setId() // win or fail?
    var token: String? = getToken() ?: setToken()

    fun getId(): Int? {
        val sql = """
            select id from player_id where uuid = ?;
        """.trimIndent()

        BlueFox.db.prepareStatement(sql).use { statement ->
            statement.setString(1, uuid.toString().replace("-", ""))
            statement.executeQuery().use { result ->
                return if (result.next()) {
                    result.getInt("id")
                } else {
                    null
                }
            }
        }
    }

    fun setId(id: Int? = null): Int? {
        return try {
            if (id == null) {
                // let MySQL auto-generate ID
                val sql = """
                INSERT INTO player_id (uuid) 
                VALUES (?)
                ON DUPLICATE KEY UPDATE uuid = uuid
            """.trimIndent()

                BlueFox.db.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, uuid.toString())
                    stmt.executeUpdate()
                }
                getId()
            } else {
                // try to insert with a specific ID
                val sql = """
                INSERT INTO player_id (id, uuid) 
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE uuid = uuid
            """.trimIndent()

                BlueFox.db.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, id)
                    stmt.setString(2, uuid.toString())
                    val affected = stmt.executeUpdate()
                    affected > 0
                }
                id
            }
        } catch (_: java.sql.SQLIntegrityConstraintViolationException) {
            // happens if the ID is already used
            null
        }
    }

    fun getToken(): String? {
        val id = id ?: return null
        val now = java.sql.Date.valueOf(java.time.LocalDate.now())

        val sql = """
            select token, expires from player_token where id = ?;
        """.trimIndent()

        BlueFox.db.prepareStatement(sql).use { statement ->
            statement.setInt(1, id)
            statement.executeQuery().use { result ->
                if (result.next()) {
                    var token =  result.getString("token")
                    val expires = result.getDate("expires")
                    if(expires.before(now)) {
                        token = BlueFox.generateToken()
                        setToken(token)
                    }
                    return token
                } else {
                    return null
                }
            }
        }
    }

    fun setToken(token: String? = null): String? {
        val id = id ?: return null
        val newToken = token ?: BlueFox.generateToken()
        val date = java.sql.Date.valueOf(java.time.LocalDate.now().plusDays(30))
        return try {
            if (token == null) {
                val sql = """
                INSERT INTO player_token (id, token, expires) 
                VALUES (?,?,?)
            """.trimIndent()

                BlueFox.db.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, id)
                    stmt.setString(2, newToken)
                    stmt.setDate(3, date)
                    stmt.executeUpdate()
                }
            } else {
                // try to insert with a specific token
                val sql = """
                INSERT INTO player_token (id, token, expires) 
                VALUES (?,?,?)
                ON DUPLICATE KEY UPDATE id = id
            """.trimIndent()

                BlueFox.db.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, id)
                    stmt.setString(2, newToken)
                    stmt.setDate(3, date)
                    val affected = stmt.executeUpdate()
                    affected > 0
                }
            }
            newToken
        } catch (_: java.sql.SQLIntegrityConstraintViolationException) {
            // happens if something or other
            null
        }
    }
}