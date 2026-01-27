package at.phactum.vortex.phase

import at.phactum.vortex.phase.exception.PhaseException
import at.phactum.vortex.phase.model.Block
import at.phactum.vortex.phase.model.Page
import at.phactum.vortex.phase.parser.ParsedPage
import at.phactum.vortex.phase.parser.Parser
import at.phactum.vortex.phase.parser.processor.Processor
import at.phactum.vortex.phase.renderer.impl.HTMLRenderer
import at.phactum.vortex.phase.renderer.impl.LinkedStyleSheet
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.slf4j.LoggerFactory
import java.nio.file.Files
import kotlin.io.path.Path

fun main() {
    val logger = LoggerFactory.getLogger(object {}.javaClass)
    val source = Files.readString(Path("grammar.pdoc"))
    val page: ParsedPage
    try {
        page = Parser(source, "grammar.pdoc").parse()
    } catch (e: PhaseException) {
        logger.error(e.formattedMessage())
        return
    }
    val root: Page
    try {
        root = Processor().processPage(page)
    } catch (e: PhaseException) {
        logger.error(e.formattedMessage())
        return
    }
    val renderer = DocumentationBuilder()
    renderer.registerRenderer(HTMLRenderer(
        LinkedStyleSheet("style.css")
    ))

    println(
        ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .writeValueAsString(
                root
            )
    )

    println(renderer.render(root).values.last())
}