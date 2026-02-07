package at.phactum.vortex.phase.processor

import at.phactum.vortex.phase.api.DirectiveType
import at.phactum.vortex.phase.api.base.AstProcessor
import at.phactum.vortex.phase.api.base.DirectiveProcessor
import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.api.exception.Position
import at.phactum.vortex.phase.api.exception.ProcessorException
import at.phactum.vortex.phase.api.model.*
import at.phactum.vortex.phase.api.model.tree.*

class StandardProcessor(override val logger: Logger) : AstProcessor(logger) {
    private val processors = mutableMapOf<DirectiveType, DirectiveProcessor>()

    private val functions = mutableMapOf<String, DefinedFunction>()

    init {
        registerProcessor(DirectiveType.SECTION, SectionProcessor(this))
        registerProcessor(DirectiveType.TABLE, TableProcessor(this))
        registerProcessor(DirectiveType.FUNCTION_DEFINITION, FunctionDefinitionProcessor(this))
    }

    private fun registerProcessor(type: DirectiveType, processor: DirectiveProcessor) {
        processors[type] = processor
    }

    override fun process(nodes: List<AstNode>): List<RenderNode> {
        return nodes.map { process(it) }
    }

    override fun process(page: ParseResult.ParsedPage): Page {
        val rootBlock = process(page.rootBlock)
        if (rootBlock !is RenderNode.Container)
            throw ProcessorException("Page is expected to contain a top-level block. This should not happen")

        return Page(
            page.file,
            processMetadata(page.metadataBlock),
            rootBlock
        )
    }

    // Turn a node into its respective element in the rendering tree
    override fun process(node: AstNode): RenderNode {
        if (node is TextualNode.PlainTextNode) {
            return TextualRenderNode.PlainText(node.text)
        }

        if (node is TextualNode.TextRunNode) {
            return TextualRenderNode.TextRun(process(node.components).map {
                if (it !is TextualRenderNode) {
                    throw ProcessorException(
                        "Text run component is required to be textual, got ${it.javaClass.simpleName}",
                        Position(node.line, node.column)
                    )
                }

                it
            })
        }

        if (node is AstNode.BlockNode) {
            val container = RenderNode.Container(process(node.nodes))
            return container
        }

        if (node is TextualNode.CallNode)
            return process(invokeFunction(node.functionIdentifier, node.parameterValue))

        val directive = (node as? AstNode.DirectiveNode)
            ?: throw ProcessorException("Unexpected node ${node.javaClass.simpleName}")

        if (directive.type == DirectiveType.BLOCK) {
            return RenderNode.Container(
                process(
                    directive.block.nodes
                        .filter { !(it is AstNode.DirectiveNode && it.type == DirectiveType.META) }
                )
            )
        }

        if (directive.type == DirectiveType.END) {
            throw ProcessorException("Unexpected ${DirectiveType.END.prefixed()} directive")
        }

        for ((type, transformer) in processors) {
            if (directive.type == type)
                return transformer.process(directive)
        }

        throw ProcessorException(
            "Directive ${directive.type.prefixed()} not allowed here",
            Position(node.line, node.column)
        )
    }

    private fun processMetadata(metadataDirective: AstNode.DirectiveNode): ProjectMetadata {
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

    override fun processProjectSettings(block: AstNode.DirectiveNode): ProjectSettings {
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
        block: AstNode.DirectiveNode,
        uniqueFields: List<DirectiveType>,
        repeatingFields: List<DirectiveType>
    ): Schema {
        val uniqueSchemaFields = mutableMapOf<DirectiveType, Field>()
        val repeatingSchemaFields = mutableListOf<Pair<DirectiveType, Field>>()

        block.block.nodes.forEach { field ->
            if (field !is AstNode.DirectiveNode)
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

    override fun defineFunction(
        specification: DirectiveInlineValueNode.InlineFunctionSpecNode,
        block: AstNode.BlockNode
    ) {
        if (functions.put(specification.functionName, DefinedFunction(specification, block)) == null)
            return

        throw ProcessorException("Function was already declared before: ${specification.functionName}")
    }

    override fun invokeFunction(
        functionIdentifier: String,
        parameter: AstNode
    ): AstNode {
        val function =
            functions[functionIdentifier] ?: throw ProcessorException("Function $functionIdentifier not found")

        val children = mutableListOf<AstNode>()

        return parameter
    }


}