package at.phactum.vortex.phase.consolidator.impl

import at.phactum.vortex.phase.RenderedPage
import at.phactum.vortex.phase.consolidator.TreeAttachment
import at.phactum.vortex.phase.consolidator.TreeBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.io.File

data class NavBarRecord(val title: String, val path: String)

open class HtmlTreeBuilder : TreeBuilder() {

    companion object {
        val NAV_INDEX_FILE = "nav_index.js"
    }

    private val mapper = ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)

    private fun pageFilename(number: Int) = "page_$number.html"

    override fun buildOutputTree(
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
            outputDirectory, TreeAttachment.StringAttachment(
                NAV_INDEX_FILE,
                """
                    |export const pages = ${mapper.writeValueAsString(record)};
                """.trimMargin()
            )
        )
    }
}