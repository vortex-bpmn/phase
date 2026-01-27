package at.phactum.vortex.phase.parser

import java.io.File

data class ParsedPage(
    val file: File,
    val metadataBlock: DirectiveNode,
    val rootBlock: Node
)

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