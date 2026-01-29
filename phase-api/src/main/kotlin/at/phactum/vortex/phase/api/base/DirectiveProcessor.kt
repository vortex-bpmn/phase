package at.phactum.vortex.phase.api.base

import at.phactum.vortex.phase.api.contract.AstProcessor
import at.phactum.vortex.phase.api.model.DirectiveNode
import at.phactum.vortex.phase.api.model.Element

abstract class DirectiveProcessor(val parentProcessor: AstProcessor) {
    abstract fun process(node: DirectiveNode): Element
}