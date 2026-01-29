package at.phactum.vortex.phase.api.model

import at.phactum.vortex.phase.api.DirectiveType
import java.io.File

sealed class ParseResult {
    data class ParsedPage(
        val file: File,
        val metadataBlock: DirectiveNode,
        val rootBlock: Node
    ) : ParseResult()

    data class ParsedProjectSettings(
        val file: File,
        val rootBlock: Node
    ) : ParseResult()
}

open class Node(open val line: Int, open val column: Int)

data class DirectiveNode(
    override val line: Int,
    override val column: Int,
    val type: DirectiveType,
    val value: String,
    val body: MutableList<Node>
) : Node(line, column)

data class TextNode(
    override val line: Int,
    override val column: Int,
    val text: String
) : Node(line, column)