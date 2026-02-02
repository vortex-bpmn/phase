package at.phactum.vortex.phase.cli

import at.phactum.vortex.phase.api.Constants
import at.phactum.vortex.phase.api.base.Pipeline
import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.api.exception.PhaseException
import at.phactum.vortex.phase.api.model.ProjectSettings
import at.phactum.vortex.phase.api.model.ProjectStructure
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

class PhaseCommand : CliktCommand() {
    override fun help(context: Context): String {
        return "Phactum Phase command line interface"
    }

    override fun run() = Unit
}

class InitCommand(val pipeline: Pipeline, val logger: Logger) : CliktCommand(name = "init") {
    val dir by argument(name = "directory", help = "New project directory")
        .file(mustExist = false)

    val name by option(help = "Project name").default("New Project")

    override fun run() {
        if (dir.exists()) {
            logger.error("Directory $dir already exists.")
            return
        }

        val creatingProject = logger.working("Creating project $dir")

        dir.mkdirs()
        File(dir, "${dir.name}.${Constants.PHASE_PROJECT_EXTENSION}")
            .writeText(
                "@project $name"
            )
        File(dir, "pages").mkdirs()

        File(dir, "pages/main.${Constants.PHASE_PAGE_EXTENSION}").writeText(
            """
            |@meta
            |    @title $name
            |    @author ${System.getProperty("user.name") ?: "A Phase User"}
            |    @version 1.0
            |@end
            |
            |@section Welcome
            |   Welcome to Phase!
            |@end
            """.trimMargin()
        )

        logger.resolve(creatingProject)
    }
}

class CreatePage(val pipeline: Pipeline, val logger: Logger) : CliktCommand(name = "page") {
    val fileName by argument(help = "File name without extension")

    val title by option("-t", "--title", help = "Title for the new page").required()
    val author by option("-a", "--author", help = "Author of the new page").required()
    val version by option("-v", "--version", help = "Version of the new page").required()

    override fun run() {
        val createPage = logger.working("Create page $fileName titled \"$title\" by \"$author\" V$version")
        File("$fileName.${Constants.PHASE_PAGE_EXTENSION}").writeText(
            """
                |@meta
                |    @title $title
                |    @author $author
                |    @version $version
                |@end
                |
                |Hello, World!
            """.trimMargin()
        )
        logger.resolve(createPage)
    }
}

class InspectCommand(val pipeline: Pipeline, val logger: Logger) : CliktCommand(name = "inspect") {
    val projectDir by argument(name = "project", help = "Project source directory")
        .file(mustExist = true, canBeDir = true, canBeFile = false)

    override fun help(context: Context): String {
        return "Inspect project structure and project configuration"
    }

    override fun run() {
        if (!projectDir.exists()) {
            logger.error("Project directory $projectDir does not exist")
            return
        }

        if (!projectDir.isDirectory) {
            logger.error("Project directory $projectDir is not a directory")
            return
        }

        val inspectingProject = logger.working("Inspecting project: ${projectDir.path}")
        val structure: ProjectStructure
        val settings: ProjectSettings
        try {
            val (struct, sett) = pipeline.scanProjectStructureAndParseSettings(projectDir)
            structure = struct
            settings = sett
        } catch (e: PhaseException) {
            logger.error(e.formattedMessage())
            return
        }
        logger.info("Project name: ${settings.name}")
        if (settings.attachments.isNotEmpty()) {
            logger.info("Attachments (${settings.attachments.size}):")
            val maxSource = settings.attachments.maxOf { it.source.length }
            settings.attachments.forEachIndexed { index, it ->
                val isLast = index == settings.attachments.size - 1
                val treeCharacter: String = if (isLast) {
                    "└──"
                } else {
                    "├──"
                }
                logger.info("$treeCharacter ${it.source.padEnd(maxSource)} -> ${it.destination}")
            }
        } else {
            logger.info("This project does not contain any custom attachments")
        }

        if (structure.pageFiles.isNotEmpty()) {
            logger.info("Pages (${structure.pageFiles.size}):")
            structure.pageFiles.forEachIndexed { index, it ->
                val isLast = index == structure.pageFiles.size - 1
                val treeCharacter: String = if (isLast) {
                    "└──"
                } else {
                    "├──"
                }
                logger.info("$treeCharacter ${it.relativeTo(projectDir).path}")
            }
        } else {
            logger.info("This project does not contain any pages")
        }

        logger.resolve(inspectingProject)
    }
}

class BuildCommand(val pipeline: Pipeline, val logger: Logger) : CliktCommand(name = "build") {
    val projectDir by argument(name = "project", help = "Project source directory")
        .file(mustExist = true, canBeDir = true, canBeFile = false)

    val output by option("--output", "-o", help = "Build output directory")
        .file(mustExist = false, canBeFile = false, canBeDir = true)
        .required()

    override fun help(context: Context): String {
        return "Compile a Phase project"
    }

    override fun run() {
        if (!projectDir.exists()) {
            logger.error("Project directory $projectDir does not exist.")
            return
        }

        if (!projectDir.isDirectory) {
            logger.error("Project directory $projectDir is not a directory.")
            return
        }

        if (output.exists()) {
            logger.error("Output directory already exists: $output")
            return
        }

        try {
            pipeline.buildProject(projectDir, output)
        } catch (e: PhaseException) {
            logger.error(e.formattedMessage())
        }
    }
}
