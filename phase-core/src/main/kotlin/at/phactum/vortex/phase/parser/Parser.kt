package at.phactum.vortex.phase.parser

import at.phactum.vortex.phase.Constants.LINE_SEPARATOR
import at.phactum.vortex.phase.exception.SyntaxException
import at.phactum.vortex.phase.parser.DirectiveType.entries
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

enum class ParsingContextType(val displayName: String) {
    PAGE_FILE("page file"),
    PROJECT_FILE("project file"),
}

class Parser(source: String, val file: File, val contextType: ParsingContextType) {
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

    companion object {
        fun parsePage(file: File): ParseResult.ParsedPage {
            val parser = Parser(
                file.readText(StandardCharsets.UTF_8),
                file,
                ParsingContextType.PAGE_FILE
            )
            val result = parser.parse()
            return result as ParseResult.ParsedPage
        }

        fun parseProjectSettings(file: File): ParseResult.ParsedProjectSettings {
            val parser = Parser(
                file.readText(StandardCharsets.UTF_8),
                file,
                ParsingContextType.PROJECT_FILE
            )
            val result = parser.parse()
            return result as ParseResult.ParsedProjectSettings
        }
    }

    fun parse(): ParseResult {
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

        if (metadataBlock == null && contextType == ParsingContextType.PAGE_FILE) {
            throw SyntaxException("Page must have a metadata block", file, lineNumber, columnNumber)
        }

        return when (contextType) {
            ParsingContextType.PAGE_FILE -> ParseResult.ParsedPage(
                file,
                metadataBlock!!,
                scopes.first()
            )

            ParsingContextType.PROJECT_FILE -> ParseResult.ParsedProjectSettings(
                file,
                scopes.first()
            )
        }
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
        while (isInBounds() && currentChar?.isLetter() ?: false || currentChar == '_' || currentChar == '-') {
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

        // Comment
        if (c == '#') {
            return
        }

        val node = TextNode(lineNumber, position, line.substring(position))

        // Add the text node to the body of the current scope
        scopes.peek().body.add(node)
    }

    private fun parseDirectiveType(s: String): DirectiveType {
        return entries.find { d -> d.identifier == s && (d.availableIn == null || d.availableIn == contextType) }
            ?: throw SyntaxException(
                "Unknown directive \"$s\" in a ${contextType.displayName}",
                file,
                lineNumber,
                columnNumber
            )
    }

    private fun parseDirective(line: String) {
        val directiveColumn = columnNumber - 1
        val identifier = tokenizeIdentifier()
        val directiveType = parseDirectiveType(identifier)

        if (directiveType == DirectiveType.END) {
            if (scopes.size <= 1)
                throw SyntaxException(
                    "No block to end here. Too many @end directives",
                    file,
                    lineNumber,
                    directiveColumn
                )

            // Register the metadata block for this page
            val lastBlock = scopes.pop()
            if (lastBlock.type == DirectiveType.META) {
                if (metadataBlock != null)
                    throw SyntaxException(
                        "Page can only have a single metadata block",
                        file,
                        lineNumber,
                        directiveColumn
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

        val node = DirectiveNode(lineNumber, directiveColumn, directiveType, value, mutableListOf())
        scopes.peek().body.add(node)

        if (directiveType.isCompound)
            scopes.push(node)
    }
}