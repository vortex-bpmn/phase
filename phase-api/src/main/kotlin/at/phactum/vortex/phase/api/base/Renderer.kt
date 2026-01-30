package at.phactum.vortex.phase.api.base

import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.api.exception.RendererException
import at.phactum.vortex.phase.api.model.Block
import at.phactum.vortex.phase.api.model.Element
import at.phactum.vortex.phase.api.model.ProjectMetadata
import at.phactum.vortex.phase.api.model.Section
import at.phactum.vortex.phase.api.model.Table
import at.phactum.vortex.phase.api.model.Text

abstract class Renderer(open val logger: Logger) {
    fun render(projectMetadata: ProjectMetadata, element: Element): String {
        val result = when (element) {
            is Section -> renderSection(projectMetadata, element);
            is Text -> renderText(projectMetadata, element)
            is Table -> renderTable(projectMetadata, element);
            is Block -> renderBlock(projectMetadata, element);
            else -> throw RendererException("Unsupported element ${element.javaClass.simpleName}")
        }
        return result
    }

    abstract fun preamble(projectMetadata: ProjectMetadata): String
    abstract fun postamble(projectMetadata: ProjectMetadata): String

    abstract fun renderTitle(projectMetadata: ProjectMetadata): String
    abstract fun renderSection(projectMetadata: ProjectMetadata, section: Section): String
    abstract fun renderText(projectMetadata: ProjectMetadata, text: Text): String
    abstract fun renderTable(projectMetadata: ProjectMetadata, table: Table): String
    abstract fun renderBlock(projectMetadata: ProjectMetadata, block: Block): String

    abstract fun postProcess(projectMetadata: ProjectMetadata, result: String): String
}