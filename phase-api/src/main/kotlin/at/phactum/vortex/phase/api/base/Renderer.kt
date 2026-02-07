package at.phactum.vortex.phase.api.base

import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.api.exception.RendererException
import at.phactum.vortex.phase.api.model.ProjectMetadata
import at.phactum.vortex.phase.api.model.tree.RenderNode
import at.phactum.vortex.phase.api.model.tree.TextualRenderNode

abstract class Renderer(open val logger: Logger) {
    fun render(projectMetadata: ProjectMetadata, node: RenderNode): String {
        val result = when (node) {
            is RenderNode.Section -> renderSection(projectMetadata, node)
            is TextualRenderNode.TextRun -> renderTextRun(projectMetadata, node)
            is TextualRenderNode.PlainText -> renderPlainText(projectMetadata, node)
            is TextualRenderNode.StyledText -> renderStyledText(projectMetadata, node)
            is RenderNode.Table -> renderTable(projectMetadata, node)
            is RenderNode.Container -> renderContainer(projectMetadata, node)
            is RenderNode.Empty -> renderEmpty(projectMetadata)
            else -> throw RendererException("Unsupported element ${node.javaClass.simpleName}")
        }
        return result
    }

    abstract fun preamble(projectMetadata: ProjectMetadata): String
    abstract fun postamble(projectMetadata: ProjectMetadata): String

    abstract fun renderTitle(projectMetadata: ProjectMetadata): String
    abstract fun renderSection(projectMetadata: ProjectMetadata, section: RenderNode.Section): String
    abstract fun renderTextRun(projectMetadata: ProjectMetadata, textRun: TextualRenderNode.TextRun): String
    abstract fun renderPlainText(projectMetadata: ProjectMetadata, text: TextualRenderNode.PlainText): String
    abstract fun renderStyledText(projectMetadata: ProjectMetadata, styledText: TextualRenderNode.StyledText): String
    abstract fun renderTable(projectMetadata: ProjectMetadata, table: RenderNode.Table): String
    abstract fun renderContainer(projectMetadata: ProjectMetadata, container: RenderNode.Container): String
    open fun renderEmpty(projectMetadata: ProjectMetadata): String {
        return ""
    }

    abstract fun postProcess(projectMetadata: ProjectMetadata, result: String): String
}