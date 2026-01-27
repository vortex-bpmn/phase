package at.phactum.vortex.phase

import at.phactum.vortex.phase.model.Metadata
import java.io.File

data class RenderedPage(
    val output: String,
    val metadata: Metadata,
    val projectRoot: File,
    val pageFile: File
)