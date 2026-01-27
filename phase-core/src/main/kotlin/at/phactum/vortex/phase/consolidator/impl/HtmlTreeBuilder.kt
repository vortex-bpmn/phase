package at.phactum.vortex.phase.consolidator.impl

import at.phactum.vortex.phase.consolidator.TreeBuilder
import at.phactum.vortex.phase.RenderedPage
import java.io.File
import java.util.UUID

open class HtmlTreeBuilder : TreeBuilder() {
    override fun buildOutputTree(
        pages: List<RenderedPage>,
        outputDirectory: File
    ) {
        pages.forEachIndexed { index, page ->
            File(outputDirectory, "page_$index.html").writeText(page.output)
        }
    }
}