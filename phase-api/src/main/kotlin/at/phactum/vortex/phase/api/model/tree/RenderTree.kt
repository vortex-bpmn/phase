package at.phactum.vortex.phase.api.model.tree

import at.phactum.vortex.phase.api.model.TextStyle

sealed class RenderNode {
    data class Container(val body: List<RenderNode>) : RenderNode()

    data class Section(val title: String, val number: Int, val body: Container) : RenderNode()

    data class Table(val rows: List<Row>) : RenderNode()
    data class Row(val columns: MutableList<Column>) : RenderNode()
    data class Column(val body: Container) : RenderNode()

    data object Empty : RenderNode()
}

sealed class TextualRenderNode : RenderNode() {
    data class TextRun(val components: List<TextualRenderNode>) : TextualRenderNode()

    data class PlainText(val text: String) : TextualRenderNode()
    data class StyledText(val text: String, val style: TextStyle) : TextualRenderNode()
}
