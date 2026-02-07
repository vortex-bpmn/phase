package at.phactum.vortex.phase.api.model

import at.phactum.vortex.phase.api.exception.ProcessorException
import at.phactum.vortex.phase.api.model.tree.AstNode
import at.phactum.vortex.phase.api.model.tree.DirectiveInlineValueNode
import at.phactum.vortex.phase.api.model.tree.RenderNode
import java.io.File

data class Project(val pages: List<RenderedPage>, val settings: ProjectSettings)

data class RenderedPage(
    val page: Page,
    val output: String,
    val projectMetadata: ProjectMetadata,
    val projectRoot: File,
    val pageFile: File
)

data class Page(val file: File, val projectMetadata: ProjectMetadata, val root: RenderNode.Container)

data class ProjectSettings(val name: String, val attachments: List<Attachment>)

data class Attachment(val source: String, val destination: String)

data class ProjectMetadata(val title: String, val author: String, val version: String)

data class DefinedFunction(
    val specification: DirectiveInlineValueNode.InlineFunctionSpecNode,
    val block: AstNode.BlockNode
)

data class TextColor(
    val red: Int,
    val green: Int,
    val blue: Int
) {
    companion object {
        fun fromHex(hexStr: String): TextColor {
            val hex = hexStr.removePrefix("#")
            return try {
                TextColor(
                    hex.substring(0, 2).toInt(16),
                    hex.substring(2, 4).toInt(16),
                    hex.substring(4, 6).toInt(16),
                )
            } catch (e: Exception) {
                throw ProcessorException("Invalid hex string: $hexStr")
            }
        }
    }
}

data class TextStyle(
    val bold: Boolean,
    val italic: Boolean,
    val underline: Boolean,
    val strikethrough: Boolean,
    val color: TextColor? = null
)