package at.phactum.vortex.phase.processor

import at.phactum.vortex.phase.api.base.DirectiveProcessor
import at.phactum.vortex.phase.api.exception.ProcessorException
import at.phactum.vortex.phase.api.model.tree.AstNode
import at.phactum.vortex.phase.api.model.tree.DirectiveInlineValueNode
import at.phactum.vortex.phase.api.model.tree.RenderNode

class SectionProcessor(override val parentProcessor: StandardProcessor) : DirectiveProcessor(parentProcessor) {

    private var sectionNumber = 1;

    override fun process(node: AstNode.DirectiveNode): RenderNode {
        val title = node.value
        if (title !is DirectiveInlineValueNode.InlineTextNode)
            throw ProcessorException(
                "Unexpected inline value type ${node.value.javaClass.simpleName} for section"
            )

        val body = parentProcessor.process(node.block.nodes)
        return RenderNode.Section(title.value, sectionNumber++, RenderNode.Container(body))
    }
}