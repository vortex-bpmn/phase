package at.phactum.vortex.phase.processor

import at.phactum.vortex.phase.api.DirectiveType
import at.phactum.vortex.phase.api.base.DirectiveProcessor
import at.phactum.vortex.phase.api.exception.Position
import at.phactum.vortex.phase.api.exception.ProcessorException
import at.phactum.vortex.phase.api.model.tree.AstNode
import at.phactum.vortex.phase.api.model.tree.DirectiveInlineValueNode
import at.phactum.vortex.phase.api.model.tree.RenderNode
import at.phactum.vortex.phase.api.model.tree.TextualRenderNode

class TableProcessor(override val parentProcessor: StandardProcessor) : DirectiveProcessor(parentProcessor) {
    override fun process(node: AstNode.DirectiveNode): RenderNode {
        val rows = mutableListOf<AstNode.DirectiveNode>()
        for (n in node.block.nodes) {
            if (n !is AstNode.DirectiveNode)
                throw ProcessorException("${n.javaClass.simpleName} is not allowed directly in a table")

            if (n.type != DirectiveType.TABLE_ROW)
                throw ProcessorException("${n.type} is not allowed directly in a table. Expected zero or more ${DirectiveType.TABLE_ROW.prefixed()}")

            rows.add(n)
        }

        val rowElements = processRows(rows)
        return RenderNode.Table(rowElements)
    }

    private fun processRows(rows: List<AstNode.DirectiveNode>): List<RenderNode.Row> {
        val rowElements = mutableListOf<RenderNode.Row>()

        for (row in rows) {
            val rowElement = RenderNode.Row(mutableListOf())

            for (col in row.block.nodes) {
                if (col !is AstNode.DirectiveNode)
                    throw ProcessorException(
                        "${col.javaClass.simpleName} is not allowed directly in a table",
                        Position(col.line, col.column)
                    )

                if (col.type != DirectiveType.TABLE_COLUMN && col.type != DirectiveType.TABLE_INLINE_COLUMN)
                    throw ProcessorException(
                        "${col.type} is not allowed directly in a row. Expected zero or more ${DirectiveType.TABLE_COLUMN.prefixed()} or ${DirectiveType.TABLE_INLINE_COLUMN.prefixed()}",
                        Position(col.line, col.column)
                    )

                if (col.value !is DirectiveInlineValueNode.InlineTextNode)
                    throw ProcessorException(
                        "Unexpected inline value type ${col.value.javaClass.simpleName} in a column",
                        col.position()
                    )

                val columnBody = if (col.type == DirectiveType.TABLE_COLUMN)
                    parentProcessor.process(col.block.nodes)
                else
                    listOf(TextualRenderNode.PlainText((col.value as DirectiveInlineValueNode.InlineTextNode).value))

                val column = RenderNode.Column(RenderNode.Container(columnBody))
                rowElement.columns.add(column)
            }

            rowElements.add(rowElement)
        }

        return rowElements
    }
}