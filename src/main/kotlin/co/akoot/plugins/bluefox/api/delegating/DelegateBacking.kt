package co.akoot.plugins.bluefox.api.delegating

import org.bukkit.plugin.Plugin
import kotlin.reflect.KType

interface DelegateBacking {
    fun <T> get(plugin: Plugin, key: String, type: KType?): T?
    fun <T> set(plugin: Plugin, key: String, value: T?)
    fun getList(plugin: Plugin, key: String, type: KType?): List<*>?
    fun setList(plugin: Plugin, key: String, list: List<*>, elementType: KType?)
}