package at.phactum.vortex.phase.cli

import at.phactum.vortex.phase.MordantLogger
import at.phactum.vortex.phase.api.exception.PhaseException
import at.phactum.vortex.phase.vortex.VortexHtmlPipeline
import java.io.File

fun registerCommands(args: Array<String>) {
    val pipeline = VortexHtmlPipeline(MordantLogger())
    val logger = pipeline.logger
    try {
//        PhaseCommand()
//            .subcommands(BuildCommand(pipeline, logger))
//            .subcommands(InitCommand(pipeline, logger))
//            .subcommands(CreatePage(pipeline, logger))
//            .subcommands(InspectCommand(pipeline, logger))
//            .main(args)
        pipeline.buildProject(File("sample_project"), File("sample_output"))
    } catch (e: PhaseException) {
        logger.failAll()
        logger.error(e.formattedMessage())
    }
}

fun main(args: Array<String>) {
    registerCommands(args)
}
