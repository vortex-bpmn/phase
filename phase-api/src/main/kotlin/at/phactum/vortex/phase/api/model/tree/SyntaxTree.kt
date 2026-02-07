package at.phactum.vortex.phase.api.model.tree

import at.phactum.vortex.phase.api.DirectiveType
import at.phactum.vortex.phase.api.exception.Position
import java.io.File

sealed class ParseResult {
    data class ParsedPage(
        val file: File,
        val metadataBlock: AstNode.DirectiveNode,
        val rootBlock: AstNode
    ) : ParseResult()

    data class ParsedProjectSettings(
        val file: File,
        val rootBlock: AstNode
    ) : ParseResult()
}

sealed class AstNode(open val line: Int, open val column: Int) {
    data class DirectiveNode(
        override val line: Int,
        override val column: Int,
        val type: DirectiveType,
        val value: DirectiveInlineValueNode,
        val block: BlockNode
    ) : AstNode(line, column) {
        fun position(): Position = Position(line, column)
    }

    data class BlockNode(
        override val line: Int,
        override val column: Int,
        val nodes: MutableList<AstNode>,
        val parentBlock: BlockNode?
    ) : AstNode(line, column)
}

// Inline directive values
sealed class DirectiveInlineValueNode(override val line: Int, override val column: Int) : AstNode(line, column) {
    data class InlineTextNode(
        override val line: Int,
        override val column: Int,
        val value: String
    ) : DirectiveInlineValueNode(line, column)

    data class InlineFunctionSpecNode(
        override val line: Int,
        override val column: Int,
        val functionName: String,
        val parameterName: String
    ) : DirectiveInlineValueNode(line, column)

    data object Empty : DirectiveInlineValueNode(0, 0)
}

sealed class TextualNode(override val line: Int, override val column: Int) : AstNode(line, column) {
    data class TextRunNode(
        override val line: Int,
        override val column: Int,
        val components: List<TextualNode>
    ) : TextualNode(line, column)

    data class PlainTextNode(
        override val line: Int,
        override val column: Int,
        val text: String,
    ) : TextualNode(line, column)

    data class CallNode(
        override val line: Int,
        override val column: Int,
        val functionIdentifier: String,
        val parameterValue: TextualNode,
    ) : TextualNode(line, column)
}