package co.akoot.plugins.bluefox.util

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class DbConfig(private val connection: Connection, private val table: String, private val keyName: String, private val keyValue: String, private val andTableName: String? = null, private val andTableKeyName: String? = null) {

    private val hasAndTable = andTableName != null && andTableKeyName != null

    fun unset(key: String) {
        var statement = connection.prepareStatement("DELETE FROM ? WHERE ? = ?")
        if (hasAndTable) {
            statement = connection.prepareStatement("DELETE FROM ? WHERE ? = (SELECT ? from ? WHERE ? = ?) AND ? = ?")
            statement.setString(1, table)
            statement.setString(2, keyName)
            statement.setString(3, andTableKeyName)
            statement.setString(4, andTableName)
            statement.setString(5, andTableKeyName)
            statement.setString(6, key)
        } else {
            statement.setString(1, table)
            statement.setString(2, keyName)
            statement.setString(3, key)
        }
        statement.executeUpdate()
    }

    fun set(key: String, value: Any?) {
        if (value == null) {
            unset(key)
            return
        }
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
        statement.setString(4, keyName)
        statement.setString(5, keyValue)
        statement.executeUpdate()
    }

    private fun updateInt(key: String, statement: PreparedStatement, amount: Int = 1) {
        statement.setString(1, table)
        statement.setString(2, key)
        statement.setString(3, key)
        statement.setInt(4, amount)
        statement.setString(5, keyName)
        statement.setString(6, keyValue)
        statement.executeUpdate()
    }

    fun increment(key: String, amount: Int = 1) {
        val statement = connection.prepareStatement("UPDATE ? SET ? = ? + ? WHERE ? = ?")
        updateInt(key, statement, amount)
    }

    fun decrement(key: String, amount: Int = 1) {
        val statement = connection.prepareStatement("UPDATE ? SET ? = ? - ? WHERE ? = ?")
        updateInt(key, statement, amount)
    }

    private fun getResultSet(key: String): ResultSet {
        val statement = connection.prepareStatement("SELECT ? from ? WHERE ? = ?")
        statement.setString(1, key)
        statement.setString(2, table)
        statement.setString(3, keyName)
        statement.setString(4, keyValue)
        return statement.executeQuery()
    }

    fun get(key: String): Any? {
        val result = getResultSet(key)
        val value = if (result.next()) result.getObject(key) else null
        result.close()
        return value
    }

    fun getString(key: String): String? {
        val result = getResultSet(key)
        val value = if (result.next()) result.getString(key) else null
        result.close()
        return value
    }
}