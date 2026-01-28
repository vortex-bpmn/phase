package at.phactum.vortex.phase.treebuilder

import at.phactum.vortex.phase.exception.ConsolidatorException
import at.phactum.vortex.phase.model.Project
import at.phactum.vortex.phase.model.RenderedPage
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

    fun buildOutputTreeWithAttachments(pages: List<RenderedPage>, project: Project, outputDirectory: File, overwrite: Boolean = false) {
        if (outputDirectory.exists()) {
            if (!overwrite)
                throw ConsolidatorException("Output directory already exists: ${outputDirectory.path}")

            outputDirectory.deleteRecursively()
        }

        outputDirectory.mkdirs()

        try {
            buildAttachments(project, outputDirectory)
            buildOutputTree(project, pages, outputDirectory)
        } catch (e: Exception) {
            outputDirectory.deleteRecursively()
            throw e
        }
    }

    fun buildAttachments(project: Project, outputDirectory: File) {
        attachments.forEach { attachment ->
            createAttachment(project, outputDirectory, attachment)
        }
    }

    fun createAttachment(project: Project, outputDirectory: File, attachment: TreeAttachment) {
        log.info("Creating Attachment: ${attachment.path}")
        val processedAttachment = preProcessAttachment(project, attachment)
        val attachmentFile = File(outputDirectory, attachment.path)
        attachmentFile.parentFile?.mkdirs()
        attachmentFile.writeBytes(processedAttachment.bytes())
    }

    abstract fun buildOutputTree(project: Project, pages: List<RenderedPage>, outputDirectory: File)

    open fun preProcessAttachment(project: Project, attachment: TreeAttachment): TreeAttachment {
        if (!attachment.substitutePlaceholders) {
            return attachment
        }

        var attachmentString = attachment.bytes().toString(Charsets.UTF_8)

        mapOf(
            "PROJECT_NAME" to project.settings.name
        ).forEach { key, value ->
            val newString = attachmentString.replace("\$$key", value)
            if (attachmentString != newString) {
                log.info("Substituting placeholder $$key -> $value")
            }
            attachmentString = newString
        }

        return TreeAttachment.StringAttachment(attachment.path, attachmentString)
    }
}