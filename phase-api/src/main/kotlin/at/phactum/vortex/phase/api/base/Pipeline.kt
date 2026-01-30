package at.phactum.vortex.phase.api.base

import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.api.model.Block
import at.phactum.vortex.phase.api.model.Page
import at.phactum.vortex.phase.api.model.ProjectMetadata
import at.phactum.vortex.phase.api.model.ProjectSettings
import at.phactum.vortex.phase.api.model.ProjectStructure
import java.io.File

abstract class Pipeline(
    open val logger: Logger,
    open val projectScanner: ProjectScanner,
    open val processor: AstProcessor,
    open val renderer: Renderer,
    open val treeBuilder: TreeBuilder
) {
    abstract fun buildProject(projectDir: File, outputDir: File)
    abstract fun scanProjectStructureAndParseSettings(projectDir: File): Pair<ProjectStructure, ProjectSettings>
    abstract fun render(projectMetadata: ProjectMetadata, root: Block): String
    abstract fun render(page: Page): String
}