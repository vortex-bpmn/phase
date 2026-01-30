package at.phactum.vortex.phase.treebuilder

import at.phactum.vortex.phase.api.base.TreeAttachment
import at.phactum.vortex.phase.api.base.TreeBuilder
import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.api.model.Project
import at.phactum.vortex.phase.api.model.RenderedPage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.io.File

data class NavBarRecord(val title: String, val path: String)

open class StandardHtmlTreeBuilder(override val logger: Logger) : TreeBuilder(logger, mutableListOf()) {

    companion object {
        val NAV_INDEX_FILE = "nav_index.js"
    }

    private val mapper = ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)

    private fun pageFilename(number: Int) = "page_$number.html"

    override fun buildOutputTree(
        project: Project,
        pages: List<RenderedPage>,
        outputDirectory: File
    ) {
        val record = mutableListOf<NavBarRecord>()

        pages.forEachIndexed { index, page ->
            val filename = pageFilename(index)
            File(outputDirectory, filename).writeText(page.output)
            record.add(
                NavBarRecord(
                    page.projectMetadata.title,
                    filename
                )
            )
        }

        createAttachment(
            project, outputDirectory, TreeAttachment.StringAttachment(
                NAV_INDEX_FILE,
                """
                    |export const pages = ${mapper.writeValueAsString(record)};
                """.trimMargin()
            )
        )
    }
}