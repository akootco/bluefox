package co.akoot.plugins.bluefox.extensions

fun String.mightBeUser(fragment: String): Boolean =
    startsWith('.') == fragment.startsWith('.') && contains(fragment, true)
