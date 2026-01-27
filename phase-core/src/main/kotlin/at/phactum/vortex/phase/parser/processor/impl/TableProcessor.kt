package at.phactum.vortex.phase.parser.processor.impl

import at.phactum.vortex.phase.exception.Position
import at.phactum.vortex.phase.exception.ProcessorException
import at.phactum.vortex.phase.model.*
import at.phactum.vortex.phase.parser.DirectiveNode
import at.phactum.vortex.phase.parser.DirectiveType
import at.phactum.vortex.phase.parser.processor.AstProcessor
import at.phactum.vortex.phase.parser.processor.Processor

class TableProcessor(processor: Processor) : AstProcessor(processor) {
    override fun process(node: DirectiveNode): Element {
        val rows = mutableListOf<DirectiveNode>()
        for (n in node.body) {
            if (n !is DirectiveNode)
                throw ProcessorException("${n.javaClass.simpleName} is not allowed directly in a table")

            if (n.type != DirectiveType.ROW)
                throw ProcessorException("${n.type} is not allowed directly in a table. Expected zero or more @row")

            rows.add(n)
        }

        val rowElements = processRows(rows)
        return Table(rowElements)
    }

    private fun processRows(rows: List<DirectiveNode>): List<Row> {
        val rowElements = mutableListOf<Row>()

        for (row in rows) {
            val rowElement = Row(mutableListOf())

            for (col in row.body) {
                if (col !is DirectiveNode)
                    throw ProcessorException(
                        "${col.javaClass.simpleName} is not allowed directly in a table",
                        Position(col.line, col.column)
                    )

                if (col.type != DirectiveType.COL && col.type != DirectiveType.ICOL)
                    throw ProcessorException(
                        "${col.type} is not allowed directly in a row. Expected zero or more @col or @icol",
                        Position(col.line, col.column)
                    )

                val columnBody = if (col.type == DirectiveType.COL)
                    processor.process(col.body)
                else
                    listOf(Text(col.value))

                val column = Column(Block(columnBody))
                rowElement.columns.add(column)
            }

            rowElements.add(rowElement)
        }

        return rowElements
    }
}