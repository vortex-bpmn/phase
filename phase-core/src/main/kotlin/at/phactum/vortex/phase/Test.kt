package at.phactum.vortex.phase

import at.phactum.vortex.phase.consolidator.TreeAttachment
import at.phactum.vortex.phase.consolidator.impl.HtmlTreeBuilder
import at.phactum.vortex.phase.exception.PhaseException
import at.phactum.vortex.phase.pipeline.Pipeline
import at.phactum.vortex.phase.renderer.impl.HtmlRenderer
import org.slf4j.LoggerFactory
import java.io.File

fun main() {
    val logger = LoggerFactory.getLogger(object {}.javaClass)
    try {
        Pipeline(
            HtmlRenderer(),
            HtmlTreeBuilder().apply {
                attach(
                    TreeAttachment.ResourceAttachment(
                        "index.html",
                        "/vortex-html/index.html"
                    )
                )
                attach(
                    TreeAttachment.ResourceAttachment(
                        "style.css",
                        "/vortex-html/style.css"
                    )
                )
                attach(
                    TreeAttachment.ResourceAttachment(
                        "navigation.js",
                        "/vortex-html/navigation.js"
                    )
                )
                attach(
                    TreeAttachment.ResourceAttachment(
                        "open-color.css",
                        "/vortex-html/open-color.css"
                    )
                )
            }
        ).buildProject(File("sample_project"), File("sample_output"))
    } catch (e: PhaseException) {
        logger.error(e.formattedMessage())
    }
}