package co.akoot.plugins.bluefox.util

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists

object IOUtil {

    /**
     * Extract a file from the jar (located in src/main/resources)
     * @param loader A class loader. When using this function from another jar, it needs that class loader. Can be
     * any class that's inside the jar you want to extract from.
     * @param source The source path from within the jar (src/main/resources/[source]).
     * @param dest The destination of the extracted file.
     * @param overwrite To overwrite or not to overwrite.
     */
    fun extractFile(loader: ClassLoader, source: String, dest: Path, overwrite: Boolean = false): Boolean {
        val exists: Boolean = dest.exists()
        return try {
            if (overwrite || !exists) {
                if (!exists) dest.parent.toFile().mkdirs()
                val stream = loader.getResourceAsStream(source) ?: throw IOException()
                Files.copy(stream, dest, StandardCopyOption.REPLACE_EXISTING)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}