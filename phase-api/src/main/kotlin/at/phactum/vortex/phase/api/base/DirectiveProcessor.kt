package at.phactum.vortex.phase.api.base

import at.phactum.vortex.phase.api.base.AstProcessor
import at.phactum.vortex.phase.api.model.DirectiveNode
import at.phactum.vortex.phase.api.model.Element

abstract class DirectiveProcessor(open val parentProcessor: AstProcessor) {
    abstract fun process(node: DirectiveNode): Element
}