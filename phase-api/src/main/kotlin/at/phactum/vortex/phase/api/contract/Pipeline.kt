package at.phactum.vortex.phase.api.contract

import at.phactum.vortex.phase.api.model.Block
import at.phactum.vortex.phase.api.model.Metadata
import at.phactum.vortex.phase.api.model.Page
import java.io.File

interface Pipeline {
    fun buildProject(root: File, outputDir: File)
    fun render(metadata: Metadata, root: Block): String
    fun render(page: Page): String
}