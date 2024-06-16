package co.akoot.plugins.bluefox.util

import java.sql.Connection

class DbConfig(val connection: Connection, val table: String, val keyName: String, val keyValue: String) {

    fun set(key: String, value: Any?) {
        if (value == null) return
        val statement = connection.prepareStatement("UPDATE ? SET ? = ? WHERE ? = ?")
        statement.setString(1, table)
        statement.setString(2, key)
        when (value) {
            is String -> statement.setString(3, value)
            is Int -> statement.setInt(3, value)
            is Long -> statement.setLong(3, value)
            is Double -> statement.setDouble(3, value)
            is Float -> statement.setFloat(3, value)
        }

    }

    fun get(key: String): Any? {
        val statement = connection.prepareStatement("SELECT ? from ? WHERE ? = ?")
        statement.setString(1, key)
        statement.setString(2, table)
        statement.setString(3, keyName)
        statement.setString(4, keyValue)
        val result = statement.executeQuery()
        return if (result.next()) result.getObject(key) else null
    }

    fun getString(key: String): String? {
        val statement = connection.prepareStatement("SELECT ? from ? WHERE ? = ?")
        statement.setString(1, key)
        statement.setString(2, table)
        statement.setString(3, keyName)
        statement.setString(4, keyValue)
        val result = statement.executeQuery()
        return if (result.next()) result.getString(key) else null
    }
}