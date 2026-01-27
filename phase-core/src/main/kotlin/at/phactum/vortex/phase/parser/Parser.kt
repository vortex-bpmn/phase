package at.phactum.vortex.phase.parser

import at.phactum.vortex.phase.exception.SyntaxException
import java.io.File
import java.util.*

class Parser(source: String, val file: File) {
    companion object {
        val LINE_SEPARATOR: String = System.lineSeparator()
    }

    private var lines: Iterator<String> = source.split(LINE_SEPARATOR).iterator()
    private var currentLine: String = ""
    private var position: Int = 0

    private var metadataBlock: DirectiveNode? = null

    private val scopes = Stack<DirectiveNode>().apply {
        push(DirectiveNode(0, 0, DirectiveType.BLOCK, "", mutableListOf()))
    }

    private val currentChar: Char?
        get() = if (!isInBounds()) null else currentLine[position]

    // Info for logging
    private var lineNumber: Int = 1
    private val columnNumber: Int
        get() = position + 1

    fun parse(): ParsedPage {
        metadataBlock = null

        while (lines.hasNext()) {
            val line = lines.next()

            currentLine = line
            position = 0

            parseAny(line)
            lineNumber++
        }

        if (scopes.size > 1)
            throw SyntaxException(
                "Unexpected end of file. Some blocks were unclosed",
                file,
                lineNumber,
                columnNumber
            )

        if (metadataBlock == null)
            throw SyntaxException("Page must have a metadata block", file, lineNumber, columnNumber)

        return ParsedPage(
            file,
            metadataBlock!!,
            scopes.first()
        )
    }

    private fun isInBounds() = position < currentLine.length

    // Move on to the next character in the line
    private fun consume() {
        if (position >= currentLine.length) {
            throw RuntimeException("Position $position out of bounds for line $lineNumber")
        }

        position++
    }

    private fun skipSpaces() {
        while (isInBounds() && currentChar?.isWhitespace() ?: false) {
            consume()
        }
    }

    private fun tokenizeIdentifier(): String {
        val identifier = StringBuilder()
        while (isInBounds() && currentChar?.isLetter() ?: false || currentChar == '_') {
            identifier.append(currentChar)
            consume()
        }
        return identifier.toString()
    }

    private fun parseAny(line: String) {
        skipSpaces()

        val position = position

        // Line is empty, nothing to parse
        val c = currentChar ?: return

        // Directive
        if (c == '@') {
            consume()
            parseDirective(line)
            return
        }

        val node = TextNode(lineNumber, position, line.substring(position))

        // Add the text node to the body of the current scope
        scopes.peek().body.add(node)
    }

    private fun parseDirective(line: String) {
        val identifierColumn = columnNumber
        val identifier = tokenizeIdentifier()
        val directiveType = DirectiveType.parse(identifier, file, lineNumber, identifierColumn)

        if (directiveType == DirectiveType.END) {
            if (scopes.size <= 1)
                throw SyntaxException(
                    "No block to end here. Too many @end directives",
                    file,
                    lineNumber,
                    identifierColumn - 1
                )

            // Register the metadata block for this page
            val lastBlock = scopes.pop()
            if (lastBlock.type == DirectiveType.META) {
                if (metadataBlock != null)
                    throw SyntaxException(
                        "Page can only have a single metadata block",
                        file,
                        lineNumber,
                        identifierColumn - 1
                    )

                metadataBlock = lastBlock
            }
            return
        }

        skipSpaces()

        val value = line.substring(position)

        // Directive needs value but none was provided
        if (directiveType.hasValue && value.trim().isEmpty())
            throw SyntaxException(
                "Directive $directiveType must have an inline value",
                file,
                lineNumber,
                columnNumber
            )

        // Directive doesn't allow inline value but one was provided
        if (!directiveType.hasValue && value.trim().isNotEmpty())
            throw SyntaxException(
                "Directive $directiveType must not have an inline value",
                file,
                lineNumber,
                columnNumber
            )

        val node = DirectiveNode(lineNumber, identifierColumn - 1, directiveType, value, mutableListOf())
        scopes.peek().body.add(node)

        if (directiveType.isCompound)
            scopes.push(node)
    }
}