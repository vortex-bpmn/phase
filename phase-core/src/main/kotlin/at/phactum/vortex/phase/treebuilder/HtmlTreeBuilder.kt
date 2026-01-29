package at.phactum.vortex.phase.treebuilder

import at.phactum.vortex.phase.api.model.Project
import at.phactum.vortex.phase.api.model.RenderedPage
import at.phactum.vortex.phase.api.base.TreeAttachment
import at.phactum.vortex.phase.api.base.TreeBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.slf4j.LoggerFactory
import java.io.File

data class NavBarRecord(val title: String, val path: String)

open class HtmlTreeBuilder : TreeBuilder(mutableListOf()) {

    companion object {
        val NAV_INDEX_FILE = "nav_index.js"
    }

    private val mapper = ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)

    private val log = LoggerFactory.getLogger(javaClass)

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
                    page.metadata.title,
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