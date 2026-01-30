package at.phactum.vortex.phase.processor

import at.phactum.vortex.phase.api.base.DirectiveProcessor
import at.phactum.vortex.phase.api.model.Block
import at.phactum.vortex.phase.api.model.DirectiveNode
import at.phactum.vortex.phase.api.model.Element
import at.phactum.vortex.phase.api.model.Section

class SectionProcessor(override val parentProcessor: StandardProcessor) : DirectiveProcessor(parentProcessor) {

    private var sectionNumber = 1;

    override fun process(node: DirectiveNode): Element {
        val title = node.value
        val body = parentProcessor.process(node.body)
        return Section(title, sectionNumber++, Block(body))
    }
}