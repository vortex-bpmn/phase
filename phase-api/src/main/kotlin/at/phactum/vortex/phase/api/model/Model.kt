package at.phactum.vortex.phase.api.model

import java.io.File

sealed class Element

data class Project(val pages: List<RenderedPage>, val settings: ProjectSettings)

data class RenderedPage(
    val page: Page,
    val output: String,
    val projectMetadata: ProjectMetadata,
    val projectRoot: File,
    val pageFile: File
)

data class Page(val file: File, val projectMetadata: ProjectMetadata, val root: Block)

data class ProjectSettings(val name: String, val attachments: List<Attachment>)

data class Attachment(val source: String, val destination: String)

data class ProjectMetadata(val title: String, val author: String, val version: String)

data class Block(val body: List<Element>) : Element()

data class Section(val title: String, val number: Int, val body: Block) : Element()

data class Text(val text: String) : Element()

data class Table(val rows: List<Row>) : Element()
data class Row(val columns: MutableList<Column>) : Element()
data class Column(val body: Block) : Element()