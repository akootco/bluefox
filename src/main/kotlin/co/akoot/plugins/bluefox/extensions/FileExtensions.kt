package co.akoot.plugins.bluefox.extensions

import java.io.File

fun File.mkdirp(): File {
    mkdirs()
    return this
}

fun File.touch(mkdirs: Boolean = true): File {
    if(mkdirs) parentFile?.mkdirs()
    createNewFile()
    return this
}

fun File.touch(content: String): File {
    parentFile?.mkdirs()
    if(!exists()) {
        writeText(content)
    }
    return this
}