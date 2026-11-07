package co.akoot.plugins.bluefox.api.changelog

class Changelog(
    val id: String,
    val title: String,
    val changes: MutableList<Change> = mutableListOf(),
    val color: String? = null,
    val icon: String? = null,
    val image: String? = null
) {

    fun add(change: Change) {
        changes.add(change)
    }

}