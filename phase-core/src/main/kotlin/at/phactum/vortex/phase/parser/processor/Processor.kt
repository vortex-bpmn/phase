package at.phactum.vortex.phase.parser.processor

import at.phactum.vortex.phase.exception.Position
import at.phactum.vortex.phase.exception.ProcessorException
import at.phactum.vortex.phase.model.*
import at.phactum.vortex.phase.parser.*
import at.phactum.vortex.phase.parser.processor.impl.SectionProcessor
import at.phactum.vortex.phase.parser.processor.impl.TableProcessor

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

    fun process(page: ParseResult.ParsedPage): Page {
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
        val schema = processSchema(
            metadataDirective,
            listOf(
                DirectiveType.TITLE,
                DirectiveType.AUTHOR,
                DirectiveType.VERSION
            )
        )

        val fields = schema.uniqueFields

        return Metadata(
            fields[DirectiveType.TITLE]!!.inlineValue,
            fields[DirectiveType.AUTHOR]!!.inlineValue,
            fields[DirectiveType.VERSION]!!.inlineValue
        )
    }

    fun processProjectSettings(rootBlock: DirectiveNode): ProjectSettings {
        val attachments = mutableListOf<Attachment>()

        val schema = processSchema(
            rootBlock,
            listOf(
                DirectiveType.PROJECT_NAME
            ),
            listOf(
                DirectiveType.ATTACHMENT
            )
        )

        schema.repeatingFields.forEach { attachmentRoot ->
            val attachment = processSchema(
                attachmentRoot.second.node,
                listOf(
                    DirectiveType.ATTACHMENT_SOURCE,
                    DirectiveType.ATTACHMENT_DESTINATION
                )
            )

            val fields = attachment.uniqueFields

            attachments.add(
                Attachment(
                    fields[DirectiveType.ATTACHMENT_SOURCE]!!.inlineValue,
                    fields[DirectiveType.ATTACHMENT_DESTINATION]!!.inlineValue
                )
            )
        }

        return ProjectSettings(
            schema.uniqueFields[DirectiveType.PROJECT_NAME]!!.inlineValue,
            attachments
        )
    }

    fun processSchema(
        block: DirectiveNode,
        uniqueFields: List<DirectiveType>,
        repeatingFields: List<DirectiveType> = listOf()
    ): Schema {
        val uniqueSchemaFields = mutableMapOf<DirectiveType, Field>()
        val repeatingSchemaFields = mutableListOf<Pair<DirectiveType, Field>>()

        block.body.forEach { field ->
            if (field !is DirectiveNode)
                throw ProcessorException(
                    "Unexpected ${field.javaClass.simpleName} in schema",
                    Position(field.line, field.column)
                )

            if (uniqueFields.contains(field.type)) {
                if (uniqueSchemaFields.put(field.type, Field.from(field)) != null) {
                    throw ProcessorException(
                        "Duplicate field ${field.type} in schema",
                        Position(field.line, field.column)
                    )
                }
                return@forEach
            }

            if (repeatingFields.contains(field.type)) {
                repeatingSchemaFields.add(Pair(field.type, Field.from(field)))
                return@forEach
            }

            throw ProcessorException(
                "Unexpected directive ${field.type} in schema",
                Position(field.line, field.column)
            )
        }

        if (uniqueSchemaFields.size != uniqueFields.size) {
            throw ProcessorException(
                "Schema does not contain all required fields (${
                    uniqueFields.map { it.identifier }.joinToString(", ")
                })",
                Position(block.line, block.column)
            )
        }

        return Schema(uniqueSchemaFields, repeatingSchemaFields)
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