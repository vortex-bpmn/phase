package at.phactum.vortex.phase.consolidator

import at.phactum.vortex.phase.RenderedPage
import java.io.File

abstract class TreeBuilder {
    abstract fun buildOutputTree(pages: List<RenderedPage>, outputDirectory: File)
}