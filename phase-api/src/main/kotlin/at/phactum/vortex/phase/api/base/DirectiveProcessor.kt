package at.phactum.vortex.phase.api.base

import at.phactum.vortex.phase.api.model.tree.AstNode
import at.phactum.vortex.phase.api.model.tree.RenderNode

abstract class DirectiveProcessor(open val parentProcessor: AstProcessor) {
    abstract fun process(node: AstNode.DirectiveNode): RenderNode
}