package at.phactum.vortex.phase.consolidator.impl

import at.phactum.vortex.phase.consolidator.TreeBuilder
import at.phactum.vortex.phase.RenderedPage
import at.phactum.vortex.phase.exception.ComposerException
import java.io.File
import java.util.UUID

class HTMLConsolidator : TreeBuilder() {
    override fun buildOutputTree(
        pages: List<RenderedPage>,
        outputDirectory: File
    ) {
        if (outputDirectory.exists())
            throw ComposerException("Output directory already exists: ${outputDirectory.path}")

        outputDirectory.mkdirs()
        pages.forEach { page ->
            val id = UUID.randomUUID().toString() + ".html"
            File(outputDirectory, id).writeText(page.output)
        }
    }
}