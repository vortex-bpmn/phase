package at.phactum.vortex.phase.processor

import at.phactum.vortex.phase.api.base.DirectiveProcessor
import at.phactum.vortex.phase.api.exception.ProcessorException
import at.phactum.vortex.phase.api.model.tree.AstNode
import at.phactum.vortex.phase.api.model.tree.DirectiveInlineValueNode
import at.phactum.vortex.phase.api.model.tree.RenderNode

class FunctionDefinitionProcessor(override val parentProcessor: StandardProcessor) :
    DirectiveProcessor(parentProcessor) {

    override fun process(node: AstNode.DirectiveNode): RenderNode {
        val value = node.value

        if (node.block.parentBlock == null || node.block.parentBlock?.parentBlock != null)
            throw ProcessorException("Function definition directive ${node.type.prefixed()} must be at the top level")

        if (value !is DirectiveInlineValueNode.InlineFunctionSpecNode)
            throw ProcessorException("Invalid inline value of directive ${node.type.prefixed()}: ${node.value.javaClass.simpleName}")

        parentProcessor.defineFunction(value, node.block)
        return RenderNode.Empty
    }
}