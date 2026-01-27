package at.phactum.vortex.phase

import at.phactum.vortex.phase.pipeline.Pipeline
import at.phactum.vortex.phase.consolidator.impl.HTMLConsolidator
import at.phactum.vortex.phase.exception.PhaseException
import at.phactum.vortex.phase.renderer.impl.HTMLRenderer
import at.phactum.vortex.phase.renderer.impl.LinkedStyleSheet
import org.slf4j.LoggerFactory
import java.io.File

fun main() {
    val logger = LoggerFactory.getLogger(object {}.javaClass)
    try {
        Pipeline(
            HTMLRenderer(
                LinkedStyleSheet("style.css")
            ),
            HTMLConsolidator()
        ).buildProject(File("sample_project"), File("sample_output"))
    } catch (e: PhaseException) {
        logger.error(e.formattedMessage())
    }
}