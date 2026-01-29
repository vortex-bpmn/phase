package at.phactum.vortex.phase.api.base

import at.phactum.vortex.phase.api.exception.ConsolidatorException
import java.io.File
import java.io.InputStream

sealed class TreeAttachment(open val path: String, open val substitutePlaceholders: Boolean) {
    abstract fun bytes(): ByteArray

    data class StringAttachment(
        override val path: String,
        val string: String,
        override val substitutePlaceholders: Boolean = true
    ) : TreeAttachment(path, substitutePlaceholders) {
        override fun bytes(): ByteArray = string.toByteArray(Charsets.UTF_8)
    }

    data class InputStreamAttachment(
        override val path: String,
        val inputStream: InputStream,
        override val substitutePlaceholders: Boolean = true
    ) : TreeAttachment(path, substitutePlaceholders) {
        override fun bytes(): ByteArray = inputStream.readAllBytes()
    }

    data class ResourceAttachment(
        override val path: String,
        val resource: String,
        override val substitutePlaceholders: Boolean = true
    ) : TreeAttachment(path, substitutePlaceholders) {
        override fun bytes(): ByteArray {
            val stream = javaClass.getResourceAsStream(resource)
                ?: throw ConsolidatorException("Resource not found: $path")
            return stream.readAllBytes()
        }
    }

    data class FileAttachment(override val path: String, val file: File, override val substitutePlaceholders: Boolean) :
        TreeAttachment(path, substitutePlaceholders) {
        override fun bytes(): ByteArray = file.readBytes()
    }
}