package at.phactum.vortex.phase.consolidator

import at.phactum.vortex.phase.RenderedPage
import at.phactum.vortex.phase.exception.ConsolidatorException
import org.slf4j.LoggerFactory
import java.io.File

abstract class TreeBuilder(val attachments: MutableList<TreeAttachment> = mutableListOf()) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Attach extra files to be emitted to the output directory.
     * The attachment path is always relative to the root output directory.
     * Possible attachments:
     *
     * [TreeAttachment.StringAttachment] - Directly emit a string
     *
     * [TreeAttachment.InputStreamAttachment] - Read from an input stream
     *
     * [TreeAttachment.ResourceAttachment] - Read from a classpath resource
     *
     * [TreeAttachment.FileAttachment] - Read from a file
     */
    fun attach(attachment: TreeAttachment) {
        attachments.add(attachment)
    }

    fun buildOutputTreeWithAttachments(pages: List<RenderedPage>, outputDirectory: File, overwrite: Boolean = false) {
        if (outputDirectory.exists()) {
            if (!overwrite)
                throw ConsolidatorException("Output directory already exists: ${outputDirectory.path}")

            outputDirectory.deleteRecursively()
        }

        outputDirectory.mkdirs()

        try {
            buildAttachments(outputDirectory)
            buildOutputTree(pages, outputDirectory)
        } catch (e: Exception) {
            outputDirectory.deleteRecursively()
            throw e
        }
    }

    fun buildAttachments(outputDirectory: File) {
        attachments.forEach { attachment ->
            createAttachment(outputDirectory, attachment)
        }
    }

    fun createAttachment(outputDirectory: File, attachment: TreeAttachment) {
        log.info("Creating Attachment: ${attachment.path}")
        val attachmentFile = File(outputDirectory, attachment.path)
        attachmentFile.parentFile?.mkdirs()
        attachmentFile.writeBytes(attachment.bytes())
    }

    abstract fun buildOutputTree(pages: List<RenderedPage>, outputDirectory: File)
}