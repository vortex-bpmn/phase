package at.phactum.vortex.phase.renderer.impl

import at.phactum.vortex.phase.exception.RendererException
import at.phactum.vortex.phase.model.*
import at.phactum.vortex.phase.renderer.Renderer
import org.jsoup.Jsoup

open class StyleSheet
data class InlineStyleSheet(val style: String) : StyleSheet()
data class LinkedStyleSheet(val path: String) : StyleSheet()

class HtmlRenderer(
    val stylesheet: StyleSheet,

    val topLevelWrapperClass: String = "wrapper",

    val titleBlockWrapperClass: String = "title-block",
    val titleWrapperClass: String = "title-wrapper",
    val titleTextClass: String = "title",
    val authorWrapperClass: String = "author-wrapper",
    val authorTextClass: String = "author",

    val sectionWrapperClass: String = "section-wrapper",
    val sectionTitleClass: String = "section-title",
    val sectionBodyClass: String = "section-body",

    val textClass: String = "text",

    val tableClass: String = "table",
    val tableHeaderRowClass: String = "table-header-row",
    val tableHeaderColClass: String = "table-header-col",
    val tableBodyColClass: String = "table-col",
) : Renderer() {

    override fun preamble(metadata: Metadata): String {
        val style = if (stylesheet is LinkedStyleSheet)
            "<link rel=\"stylesheet\" href=\"${stylesheet.path}\">"
        else if (stylesheet is InlineStyleSheet)
            "<style>\n${stylesheet.style}\n</style>"
        else
            throw RendererException("Unknown style sheet: ${stylesheet.javaClass.simpleName}")

        return """
            <html>
                <head>
                    <title>${metadata.title}</title>
                    $style
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
            </div>
        """.trimIndent()
    }

    override fun renderSection(metadata: Metadata, section: Section): String {
        return """
            <div class="$sectionWrapperClass">
                <div class="$sectionTitleClass">
                   ${section.title}
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