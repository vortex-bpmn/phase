package at.phactum.vortex.phase.parser.processor.impl

import at.phactum.vortex.phase.model.Block
import at.phactum.vortex.phase.model.Element
import at.phactum.vortex.phase.model.Section
import at.phactum.vortex.phase.parser.DirectiveNode
import at.phactum.vortex.phase.parser.processor.AstProcessor
import at.phactum.vortex.phase.parser.processor.Processor

class SectionProcessor(processor: Processor) : AstProcessor(processor) {
    override fun process(node: DirectiveNode): Element {
        val title = node.value
        val body = processor.process(node.body)
        return Section(title, Block(body))
    }
}