package at.phactum.vortex.phase.renderer

import at.phactum.vortex.phase.api.exception.RendererException
import at.phactum.vortex.phase.api.model.Block
import at.phactum.vortex.phase.api.model.Metadata
import at.phactum.vortex.phase.api.model.Section
import at.phactum.vortex.phase.api.model.Table
import at.phactum.vortex.phase.api.model.Text
import at.phactum.vortex.phase.api.contract.Renderer
import org.jsoup.Jsoup

open class StyleSheet
data class InlineStyleSheet(val style: String) : StyleSheet()
data class LinkedStyleSheet(val path: String) : StyleSheet()

data class Script(val path: String, val module: Boolean = false, val defer: Boolean = true)

class HtmlRenderer(
    val stylesheet: StyleSheet? = null,
    val scripts: MutableList<Script> = mutableListOf(),

    val lang: String = "en",

    val topLevelWrapperClass: String = "wrapper",

    val titleBlockWrapperClass: String = "title-block",
    val titleWrapperClass: String = "title-wrapper",
    val titleTextClass: String = "title",
    val authorWrapperClass: String = "author-wrapper",
    val authorTextClass: String = "author",
    val versionWrapperClass: String = "version-wrapper",
    val versionTextClass: String = "version",

    val sectionWrapperClass: String = "section-wrapper",
    val sectionHeaderClass: String = "section-header",
    val sectionNumberWrapperClass: String = "section-number-wrapper",
    val sectionNumberTextClass: String = "section-number-text",
    val sectionTitleWrapperClass: String = "section-title-wrapper",
    val sectionTitleTextClass: String = "section-title",
    val sectionBodyClass: String = "section-body",

    val textClass: String = "text",

    val tableClass: String = "table",
    val tableHeaderRowClass: String = "table-header-row",
    val tableHeaderColClass: String = "table-header-col",
    val tableBodyColClass: String = "table-col",
) : Renderer {

    override fun preamble(metadata: Metadata): String {
        val style = if (stylesheet == null)
            "<!-- No Stylesheet -->"
        else if (stylesheet is LinkedStyleSheet)
            "<link rel=\"stylesheet\" href=\"${stylesheet.path}\">"
        else if (stylesheet is InlineStyleSheet)
            "<style>\n${stylesheet.style}\n</style>"
        else
            throw RendererException("Unknown style sheet: ${stylesheet.javaClass.simpleName}")

        return """
            <html lang="$lang">
                <head>
                    <title>${metadata.title}</title>
                    $style
                    ${
            scripts.map { "<script${if (it.module) " type=\"module\"" else ""} src=\"${it.path}\"${if (it.defer) " defer" else ""}></script>" }
                .joinToString("\n")
        }
                </head>
                <body>
                    <div class="$topLevelWrapperClass">
        """
    }

    override fun postamble(metadata: Metadata): String {
        return """
                    </div>
                </body>
            </html>
        """
    }

    override fun renderTitle(metadata: Metadata): String {
        return """
            <div class="$titleBlockWrapperClass">
                <div class="$titleWrapperClass">
                    <span class="$titleTextClass">${metadata.title}</span>
                </div>
                <div class="$authorWrapperClass">
                    <span class="$authorTextClass">${metadata.author}</span>
                </div>
                <div class="$versionWrapperClass">
                    <span class="$versionTextClass">${metadata.version}</span>
                </div>
            </div>
        """.trimIndent()
    }

    override fun renderSection(metadata: Metadata, section: Section): String {
        return """
            <div class="$sectionWrapperClass">
                <div class="$sectionHeaderClass">
                    <div class="$sectionNumberWrapperClass">
                        <span class="$sectionNumberTextClass">${section.number}</span>
                    </div>
                    <div class="$sectionTitleWrapperClass">
                        <span class="$sectionTitleTextClass">${section.title}</span>
                    </div>
                </div>
                <div class="$sectionBodyClass">
                   ${render(metadata, section.body)}
                </div>
            </div>
        """
    }

    override fun renderText(metadata: Metadata, text: Text): String {
        return """
            <span class="$textClass">
                ${text.text}
            </span>
        """
    }

    override fun renderTable(metadata: Metadata, table: Table): String {
        if (table.rows.isEmpty())
            return "<!-- Not rendering empty table -->"

        val output = StringBuilder(
            """
                <table class="$tableClass">
            """.trimIndent()
        )

        val expectedColCount = table.rows.first().columns.size
        for ((index, row) in table.rows.withIndex()) {
            if (row.columns.size != expectedColCount)
                throw RendererException("All rows must have the same number of columns.")

            if (index == 0)
                output.append("<thead>\n")
            else if (index == 1)
                output.append("<tbody>\n")

            // Row start
            output.append("<tr class=\"$tableHeaderRowClass\">\n")

            for (col in row.columns) {
                val colResult = render(metadata, col.body)

                if (index == 0) {
                    // First row (header)
                    output.append(
                        """
                        <th class="$tableHeaderColClass">
                            $colResult
                        </th>
                        """.trimIndent()
                    )
                } else {
                    // Other rows
                    output.append(
                        """
                            <td class="$tableBodyColClass">
                                $colResult
                            </td>
                        """.trimIndent()
                    )
                }
            }

            // Row end
            output.append("</tr>\n")

            if (index == 0)
                output.append("</thead>\n")
            else if (index == table.rows.size)
                output.append("</tbody>\n")
        }

        output.append("</table>")
        return output.toString()
    }

    override fun renderBlock(metadata: Metadata, block: Block): String {
        return block.body.map { render(metadata, it) }.joinToString("\n")
    }

    override fun postProcess(metadata: Metadata, result: String): String {
        return Jsoup.parse(result).apply {
            outputSettings()
                .prettyPrint(true)
                .indentAmount(4)
        }.html()
    }
}