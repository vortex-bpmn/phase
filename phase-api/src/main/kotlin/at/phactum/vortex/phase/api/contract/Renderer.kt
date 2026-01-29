package at.phactum.vortex.phase.api.contract

import at.phactum.vortex.phase.api.exception.RendererException
import at.phactum.vortex.phase.api.model.Block
import at.phactum.vortex.phase.api.model.Element
import at.phactum.vortex.phase.api.model.Metadata
import at.phactum.vortex.phase.api.model.Section
import at.phactum.vortex.phase.api.model.Table
import at.phactum.vortex.phase.api.model.Text

interface Renderer {
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

    fun preamble(metadata: Metadata): String
    fun postamble(metadata: Metadata): String

    fun renderTitle(metadata: Metadata): String
    fun renderSection(metadata: Metadata, section: Section): String
    fun renderText(metadata: Metadata, text: Text): String
    fun renderTable(metadata: Metadata, table: Table): String
    fun renderBlock(metadata: Metadata, block: Block): String

    fun postProcess(metadata: Metadata, result: String): String
}