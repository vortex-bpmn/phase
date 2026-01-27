package at.phactum.vortex.phase.model

import java.io.File

sealed class Element

data class Metadata(val title: String, val author: String, val version: String)

data class Page(val file: File, val metadata: Metadata, val root: Block)

data class Block(val body: List<Element>) : Element()

data class Section(val title: String, val body: Block) : Element()

data class Text(val text: String) : Element()

data class Table(val rows: List<Row>) : Element()
data class Row(val columns: MutableList<Column>) : Element()
data class Column(val body: Block) : Element()