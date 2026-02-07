package at.phactum.vortex.phase.api.model

import at.phactum.vortex.phase.api.DirectiveType
import at.phactum.vortex.phase.api.model.tree.AstNode
import at.phactum.vortex.phase.api.model.tree.DirectiveInlineValueNode

data class Field(
    val type: DirectiveType,
    val node: AstNode.DirectiveNode,
    val inlineValue: String
) {
    companion object {
        fun from(directive: AstNode.DirectiveNode): Field {
            return Field(
                directive.type,
                directive,
                if (directive.value is DirectiveInlineValueNode.InlineTextNode) directive.value.value else ""
            )
        }
    }
}

data class Schema(
    val uniqueFields: Map<DirectiveType, Field>,
    val repeatingFields: List<Pair<DirectiveType, Field>>
)