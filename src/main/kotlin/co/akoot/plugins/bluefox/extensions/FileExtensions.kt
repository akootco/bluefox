package co.akoot.plugins.bluefox.extensions

import java.io.File

fun File.mkdirp(): File {
    mkdirs()
    return this
}