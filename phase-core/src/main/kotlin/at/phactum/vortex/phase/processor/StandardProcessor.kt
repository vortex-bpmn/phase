package at.phactum.vortex.phase.processor

import at.phactum.vortex.phase.api.base.DirectiveProcessor
import at.phactum.vortex.phase.api.model.Attachment
import at.phactum.vortex.phase.api.model.Block
import at.phactum.vortex.phase.api.model.DirectiveNode
import at.phactum.vortex.phase.api.DirectiveType
import at.phactum.vortex.phase.api.base.AstProcessor
import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.api.exception.Position
import at.phactum.vortex.phase.api.exception.ProcessorException
import at.phactum.vortex.phase.api.model.Element
import at.phactum.vortex.phase.api.model.Field
import at.phactum.vortex.phase.api.model.ProjectMetadata
import at.phactum.vortex.phase.api.model.Node
import at.phactum.vortex.phase.api.model.Page
import at.phactum.vortex.phase.api.model.ParseResult
import at.phactum.vortex.phase.api.model.ProjectSettings
import at.phactum.vortex.phase.api.model.Schema
import at.phactum.vortex.phase.api.model.Text
import at.phactum.vortex.phase.api.model.TextNode
import kotlin.collections.iterator

class StandardProcessor(override val logger: Logger) : AstProcessor(logger) {
    private val processors = mutableMapOf<DirectiveType, DirectiveProcessor>()

    init {
        registerProcessor(DirectiveType.SECTION, SectionProcessor(this))
        registerProcessor(DirectiveType.TABLE, TableProcessor(this))
    }

    private fun registerProcessor(type: DirectiveType, processor: DirectiveProcessor) {
        processors[type] = processor
    }

    override fun process(nodes: List<Node>): List<Element> {
        return nodes.map { process(it) }
    }

    override fun process(page: ParseResult.ParsedPage): Page {
        val rootBlock = process(page.rootBlock)
        if (rootBlock !is Block)
            throw ProcessorException("Page is expected to contain a top-level block. This should not happen")

        return Page(
            page.file,
            processMetadata(page.metadataBlock),
            rootBlock
        )
    }

    private fun processMetadata(metadataDirective: DirectiveNode): ProjectMetadata {
        val schema = processSchema(
            metadataDirective,
            listOf(
                DirectiveType.TITLE,
                DirectiveType.AUTHOR,
                DirectiveType.VERSION
            )
        )

        val fields = schema.uniqueFields

        return ProjectMetadata(
            fields[DirectiveType.TITLE]!!.inlineValue,
            fields[DirectiveType.AUTHOR]!!.inlineValue,
            fields[DirectiveType.VERSION]!!.inlineValue
        )
    }

    override fun processProjectSettings(block: DirectiveNode): ProjectSettings {
        val attachments = mutableListOf<Attachment>()

        val schema = processSchema(
            block,
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

    override fun processSchema(
        block: DirectiveNode,
        uniqueFields: List<DirectiveType>,
        repeatingFields: List<DirectiveType>
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

    override fun process(node: Node): Element {
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