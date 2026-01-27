package at.phactum.vortex.phase.consolidator.impl

import at.phactum.vortex.phase.consolidator.TreeBuilder
import at.phactum.vortex.phase.RenderedPage
import java.io.File
import java.util.UUID

open class HtmlConsolidator : TreeBuilder() {
    override fun buildOutputTree(
        pages: List<RenderedPage>,
        outputDirectory: File
    ) {
        pages.forEach { page ->
            val id = UUID.randomUUID().toString() + ".html"
            File(outputDirectory, id).writeText(page.output)
        }
    }
}