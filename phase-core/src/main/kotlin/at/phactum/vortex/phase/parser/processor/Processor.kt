package at.phactum.vortex.phase.parser.processor

import at.phactum.vortex.phase.exception.Position
import at.phactum.vortex.phase.exception.ProcessorException
import at.phactum.vortex.phase.model.*
import at.phactum.vortex.phase.parser.*
import at.phactum.vortex.phase.parser.processor.impl.SectionProcessor
import at.phactum.vortex.phase.parser.processor.impl.TableProcessor

class Processor {
    private val processors = mutableMapOf<DirectiveType, AstProcessor>()

    init {
        registerProcessor(DirectiveType.SECTION, SectionProcessor(this))
        registerProcessor(DirectiveType.TABLE, TableProcessor(this))
    }

    private fun registerProcessor(type: DirectiveType, processor: AstProcessor) {
        processors[type] = processor
    }

    fun process(nodes: List<Node>): List<Element> {
        return nodes.map { process(it) }
    }

    fun process(page: ParsedPage): Page {
        val rootBlock = process(page.rootBlock)
        if (rootBlock !is Block)
            throw ProcessorException("Page is expected to contain a top-level block. This should not happen")

        return Page(
            page.file,
            processMetadata(page.metadataBlock),
            rootBlock
        )
    }

    private fun processMetadata(metadataDirective: DirectiveNode): Metadata {
        val fields = mutableMapOf<DirectiveType, String>()

        for (field in metadataDirective.body) {
            if (field !is DirectiveNode)
                throw ProcessorException(
                    "Unexpected ${field.javaClass.simpleName} in metadata block",
                    Position(field.line, field.column)
                )

            if (!arrayOf(DirectiveType.TITLE, DirectiveType.AUTHOR, DirectiveType.VERSION).contains(field.type))
                throw ProcessorException(
                    "Unexpected directive ${field.type} in metadata block",
                    Position(field.line, field.column)
                )

            if (fields.put(field.type, field.value) != null)
                throw ProcessorException(
                    "Duplicate field ${field.type} in metadata block",
                    Position(field.line, field.column)
                )
        }

        val presentFields = fields.keys

        if (!presentFields.contains(DirectiveType.TITLE))
            throw ProcessorException(
                "Title is required in metadata block",
                Position(metadataDirective.line, metadataDirective.column)
            )

        if (!presentFields.contains(DirectiveType.AUTHOR))
            throw ProcessorException(
                "Author is required in metadata block",
                Position(metadataDirective.line, metadataDirective.column)
            )

        if (!presentFields.contains(DirectiveType.VERSION))
            throw ProcessorException(
                "Version is required in metadata block",
                Position(metadataDirective.line, metadataDirective.column)
            )

        return Metadata(
            fields[DirectiveType.TITLE]!!,
            fields[DirectiveType.AUTHOR]!!,
            fields[DirectiveType.VERSION]!!
        )
    }

    fun process(node: Node): Element {
        if (node is TextNode)
            return Text(node.text)

        val directive = (node as? DirectiveNode)
            ?: throw ProcessorException("Unexpected node ${node.javaClass.simpleName}")

        if (directive.type == DirectiveType.BLOCK) {
            return Block(
                process(
                    directive.body
                        .filter { !(it is DirectiveNode && it.type == DirectiveType.META) }
                )
            )
        }

        if (directive.type == DirectiveType.END) {
            throw ProcessorException("Unexpected @end directive")
        }

        for ((type, transformer) in processors) {
            if (directive.type == type)
                return transformer.process(directive)
        }

        throw ProcessorException("Directive ${directive.type} not allowed here", Position(node.line, node.column))
    }
}