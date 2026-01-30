package at.phactum.vortex.phase.cli

import at.phactum.vortex.phase.MordantLogger
import at.phactum.vortex.phase.vortex.VortexHtmlPipeline
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands

fun registerCommands(args: Array<String>) {
    val pipeline = VortexHtmlPipeline(MordantLogger())
    val logger = pipeline.logger
    PhaseCommand()
        .subcommands(BuildCommand(pipeline, logger))
        .subcommands(InitCommand(pipeline, logger))
        .subcommands(CreatePage(pipeline, logger))
        .subcommands(InspectCommand(pipeline, logger))
        .main(args)
}

fun main(args: Array<String>) {
    registerCommands(args)
}
