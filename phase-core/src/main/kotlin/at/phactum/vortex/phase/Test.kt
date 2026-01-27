package at.phactum.vortex.phase

import at.phactum.vortex.phase.consolidator.TreeAttachment
import at.phactum.vortex.phase.consolidator.impl.HtmlTreeBuilder
import at.phactum.vortex.phase.exception.PhaseException
import at.phactum.vortex.phase.pipeline.Pipeline
import at.phactum.vortex.phase.renderer.impl.HtmlRenderer
import at.phactum.vortex.phase.renderer.impl.LinkedStyleSheet
import at.phactum.vortex.phase.renderer.impl.Script
import org.slf4j.LoggerFactory
import java.io.File

fun main() {
    val logger = LoggerFactory.getLogger(object {}.javaClass)
    try {
        Pipeline(
            HtmlRenderer(
                LinkedStyleSheet("style.css")
            ),
            HtmlTreeBuilder().apply {
                attach(
                    TreeAttachment.ResourceAttachment(
                        "style.css",
                        "/vortex_stylesheet.css"
                    )
                )
            }
        ).buildProject(File("sample_project"), File("sample_output"))
    } catch (e: PhaseException) {
        logger.error(e.formattedMessage())
    }
}