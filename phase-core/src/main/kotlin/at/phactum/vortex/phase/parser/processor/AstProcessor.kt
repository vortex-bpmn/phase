package at.phactum.vortex.phase.parser.processor

import at.phactum.vortex.phase.model.Element
import at.phactum.vortex.phase.parser.DirectiveNode

abstract class AstProcessor(val processor: Processor) {
    abstract fun process(node: DirectiveNode): Element
}