package at.phactum.vortex.phase.renderer

import at.phactum.vortex.phase.exception.RendererException
import at.phactum.vortex.phase.model.*

abstract class Renderer {
    fun render(metadata: Metadata, element: Element): String {
        val result = when (element) {
            is Section -> renderSection(metadata, element);
            is Text -> renderText(metadata, element)
            is Table -> renderTable(metadata, element);
            is Block -> renderBlock(metadata, element);
            else -> throw RendererException("Unsupported element ${element.javaClass.simpleName}")
        }
        return result
    }

    abstract fun preamble(metadata: Metadata): String
    abstract fun postamble(metadata: Metadata): String

    abstract fun renderTitle(metadata: Metadata): String
    abstract fun renderSection(metadata: Metadata, section: Section): String
    abstract fun renderText(metadata: Metadata, text: Text): String
    abstract fun renderTable(metadata: Metadata, table: Table): String
    abstract fun renderBlock(metadata: Metadata, block: Block): String

    abstract fun postProcess(metadata: Metadata, result: String): String
}