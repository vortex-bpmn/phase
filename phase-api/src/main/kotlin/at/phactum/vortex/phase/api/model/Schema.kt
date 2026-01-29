package at.phactum.vortex.phase.api.model

import at.phactum.vortex.phase.api.DirectiveType

data class Field(
    val type: DirectiveType,
    val node: DirectiveNode,
    val inlineValue: String
) {
    companion object {
        fun from(directive: DirectiveNode): Field = Field(
            directive.type,
            directive,
            directive.value
        )
    }
}

data class Schema(
    val uniqueFields: Map<DirectiveType, Field>,
    val repeatingFields: List<Pair<DirectiveType, Field>>
)