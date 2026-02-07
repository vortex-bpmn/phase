package at.phactum.vortex.phase.parser

import at.phactum.vortex.phase.api.Constants.LINE_SEPARATOR
import at.phactum.vortex.phase.api.DirectiveType
import at.phactum.vortex.phase.api.DirectiveType.entries
import at.phactum.vortex.phase.api.ParsingContextType
import at.phactum.vortex.phase.api.exception.SyntaxException
import at.phactum.vortex.phase.api.model.tree.AstNode
import at.phactum.vortex.phase.api.model.tree.DirectiveInlineValueNode
import at.phactum.vortex.phase.api.model.tree.ParseResult
import at.phactum.vortex.phase.api.model.tree.TextualNode
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

class Parser(source: String, val file: File, val contextType: ParsingContextType) {
    private var lines: Iterator<String> = source.split(LINE_SEPARATOR).iterator()
    private var currentLine: String = ""
    private var position: Int = 0

    private var metadataBlock: AstNode.DirectiveNode? = null

    private val scopes = Stack<AstNode.DirectiveNode>().apply {
        push(
            AstNode.DirectiveNode(
                0,
                0,
                DirectiveType.BLOCK,
                DirectiveInlineValueNode.Empty,
                AstNode.BlockNode(0, 0, mutableListOf(), null)
            )
        )
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
            syntaxError("Unexpected end of file. Some blocks were unclosed")

        if (metadataBlock == null && contextType == ParsingContextType.PAGE_FILE)
            syntaxError("Page must have a metadata block")

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

    private fun tokenizeIdentifier(): String? {
        if (!isInBounds())
            return null

        val identifier = StringBuilder()
        while (isInBounds() && currentChar?.isLetter() ?: false || currentChar == '_' || currentChar == '-') {
            identifier.append(currentChar)
            consume()
        }

        return if (identifier.isEmpty()) null else identifier.toString()
    }

    private fun parseAny(line: String) {
        skipSpaces()

        // Line is empty, nothing to parse
        val c = currentChar ?: return

        // Comment
        if (c == '#') {
            return
        }

        // Directive
        if (c == '@') {
            consume()
            parseDirective(line)
            return
        }

        // Add the text node to the body of the current scope
        val text = parseText() ?: return
        scopes.peek().block.nodes.add(text)
    }

    private fun parseText(delimiter: Char? = null): TextualNode {
        val startLine = lineNumber
        val startCol = columnNumber

        val children = mutableListOf<TextualNode>()

        val text = StringBuilder()

        fun flush() {
            if (text.isEmpty())
                return

            children += TextualNode.PlainTextNode(lineNumber, columnNumber - text.length, text.toString())
            text.clear()
        }

        while (isInBounds()) {
            val c = currentChar!!

            if (delimiter != null && c == delimiter)
                break

            if (c == '$') {
                val callCol = columnNumber

                flush()
                consume() // Skip '$'
                val functionIdentifier = tokenizeIdentifier() ?: syntaxError("Expected function name after '$'")

                (currentChar ?: syntaxError("Expected '(' after function name \"$functionIdentifier\""))
                    .expect('(', { c -> "Expected '(' after function name \"$functionIdentifier\", got '$c'" })

                consume() // Skip '('

                val parameter = parseText(delimiter = ')')
                consume()

                children += TextualNode.CallNode(startLine, callCol, functionIdentifier, parameter)
                continue
            }

            text.append(c)
            consume()
        }

        flush()
        return TextualNode.TextRunNode(startLine, startCol, children)
    }

    private fun parseDirectiveType(s: String): DirectiveType {
        return entries.find { d -> d.identifier == s && (d.availableIn == null || d.availableIn == contextType) }
            ?: syntaxError("Unknown directive \"$s\" in a ${contextType.displayName}")
    }

    private fun parseDirective(line: String) {
        val directiveColumn = columnNumber - 1
        val identifier = tokenizeIdentifier()
        val directiveType = parseDirectiveType(
            identifier ?: syntaxError("Expected directive identifier after '@'")
        )

        if (directiveType == DirectiveType.END) {
            if (scopes.size <= 1)
                throw SyntaxException(
                    "No block to end here. Too many ${DirectiveType.END.prefixed()} directives",
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

        // The value of a function definition directive specifies the function signature and needs to be parsed separately
        if (directiveType == DirectiveType.FUNCTION_DEFINITION) {
            val specification = parseFunctionSpecification()
            val node = AstNode.DirectiveNode(
                lineNumber,
                directiveColumn,
                directiveType,
                specification,
                AstNode.BlockNode(lineNumber, columnNumber, mutableListOf(), scopes.peek().block)
            )
            scopes.peek().block.nodes.add(node)
            scopes.push(node)
            return
        }

        val value = line.substring(position)

        // Directive needs value but none was provided
        if (directiveType.hasValue && value.trim().isEmpty())
            syntaxError("Directive $directiveType must have an inline value")

        // Directive doesn't allow inline value but one was provided
        if (!directiveType.hasValue && value.trim().isNotEmpty())
            syntaxError("Directive $directiveType must not have an inline value")

        val node = AstNode.DirectiveNode(
            lineNumber,
            directiveColumn,
            directiveType,
            if (value.isEmpty())
                DirectiveInlineValueNode.Empty
            else
                DirectiveInlineValueNode.InlineTextNode(lineNumber, columnNumber - value.length, value),
            AstNode.BlockNode(lineNumber, columnNumber, mutableListOf(), scopes.peek().block)
        )
        scopes.peek().block.nodes.add(node)

        if (directiveType.isCompound)
            scopes.push(node)
    }

    private fun parseFunctionSpecification(): DirectiveInlineValueNode.InlineFunctionSpecNode {
        val col = columnNumber

        skipSpaces()

        (currentChar ?: syntaxError("Expected '$' at the start of a function specification"))
            .expect('$') { c -> "Expected '$' at the start of a function specification, got '$c" }

        consume()

        val functionId = tokenizeIdentifier() ?: syntaxError("Expected function identifier after '$'")

        (currentChar ?: syntaxError("Expected '(' after the function identifier '$functionId'"))
            .expect('(') { c -> "Expected '(' after the function identifier '$functionId', got '$c'" }

        consume()

        val parameterId = tokenizeIdentifier() ?: syntaxError("Expected parameter identifier after '('")

        (currentChar ?: syntaxError("Expected ')' after the parameter identifier '$parameterId'"))
            .expect(')') { c -> "Expected ')' after parameter identifier '$parameterId', got '$c'" }

        return DirectiveInlineValueNode.InlineFunctionSpecNode(lineNumber, col, functionId, parameterId)
    }

    private fun syntaxError(message: String): Nothing = throw SyntaxException(message, file, lineNumber, columnNumber)

    private fun Char.expect(c: Char, syntaxErrorMessage: (c: Char) -> String): Char {
        if (this == c)
            return this

        syntaxError(syntaxErrorMessage(this))
    }

}