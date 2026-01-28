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

        return Metadata(
            schema[DirectiveType.TITLE]!!,
            schema[DirectiveType.AUTHOR]!!,
            schema[DirectiveType.VERSION]!!
        )
    }

    fun processProjectSettings(rootBlock: DirectiveNode): ProjectSettings {

        val attachments = mutableListOf<Attachment>()
        var projectName: String? = null

        rootBlock.body.forEach { field ->
            if (field !is DirectiveNode) {
                throw ProcessorException(
                    "Unexpected ${field.javaClass.simpleName} in schema",
                    Position(field.line, field.column)
                )
            }

            if (field.type == DirectiveType.PROJECT_NAME) {
                if (projectName != null) {
                    throw ProcessorException(
                        "Duplicate field ${field.type} in schema",
                        Position(field.line, field.column)
                    )
                }
                projectName = field.value
                return@forEach
            }

            if (field.type == DirectiveType.ATTACHMENT) {
                val attachmentModel = processSchema(
                    field,
                    listOf(
                        DirectiveType.ATTACHMENT_SOURCE,
                        DirectiveType.ATTACHMENT_DESTINATION
                    )
                )

                attachments.add(
                    Attachment(
                        attachmentModel[DirectiveType.ATTACHMENT_SOURCE]!!,
                        attachmentModel[DirectiveType.ATTACHMENT_DESTINATION]!!
                    )
                )

                return@forEach
            }

            throw ProcessorException(
                "Unexpected directive ${field.type} in schema",
                Position(field.line, field.column)
            )
        }

        return ProjectSettings(
            projectName ?: throw ProcessorException(
                "Project name not set in the project settings",
            ),
            attachments
        )
    }

    fun processSchema(block: DirectiveNode, expectedFields: List<DirectiveType>): Map<DirectiveType, String> {
        val fields = mutableMapOf<DirectiveType, String>()

        block.body.forEach { field ->
            if (field !is DirectiveNode)
                throw ProcessorException(
                    "Unexpected ${field.javaClass.simpleName} in schema",
                    Position(field.line, field.column)
                )

            if (!expectedFields.contains(field.type)) {
                throw ProcessorException(
                    "Unexpected directive ${field.type} in schema",
                    Position(field.line, field.column)
                )
            }

            if (fields.put(field.type, field.value) != null) {
                throw ProcessorException(
                    "Duplicate field ${field.type} in schema",
                    Position(field.line, field.column)
                )
            }
        }

        if (fields.size != expectedFields.size) {
            throw ProcessorException(
                "Schema does not contain all required fields (${
                    expectedFields.map { it.identifier }.joinToString(", ")
                })",
                Position(block.line, block.column)
            )
        }

        return fields
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