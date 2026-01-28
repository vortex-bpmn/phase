package at.phactum.vortex.phase

import at.phactum.vortex.phase.exception.PhaseException
import at.phactum.vortex.phase.pipeline.preset.VortexHtmlPipeline
import org.slf4j.LoggerFactory
import java.io.File

fun main() {
    val logger = LoggerFactory.getLogger(object {}.javaClass)
    try {
        VortexHtmlPipeline().buildProject(File("sample_project"), File("sample_output"))
    } catch (e: PhaseException) {
        logger.error(e.formattedMessage())
    }
}