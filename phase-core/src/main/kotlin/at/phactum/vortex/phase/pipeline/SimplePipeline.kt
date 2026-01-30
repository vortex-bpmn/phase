package at.phactum.vortex.phase.pipeline

import at.phactum.vortex.phase.api.base.*
import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.api.exception.PipelineException
import at.phactum.vortex.phase.api.model.*
import at.phactum.vortex.phase.parser.Parser
import java.io.File

open class SimplePipeline(
    override val logger: Logger,
    override val projectScanner: ProjectScanner,
    override val processor: AstProcessor,
    override val renderer: Renderer,
    override val treeBuilder: TreeBuilder
) : Pipeline(logger, projectScanner, processor, renderer, treeBuilder) {
    override fun buildProject(projectDir: File, outputDir: File) {
        val startTime = System.currentTimeMillis()

        logger.working("Examining project structure ${projectDir.path}")

        val (structure, settings) = scanProjectStructureAndParseSettings(projectDir)

        logger.working("Building project ${settings.name}")

        settings.attachments.forEach {
            val sourceFile = File(projectDir, it.source)

            if (!sourceFile.exists()) {
                throw PipelineException("Custom attachment file does not exist: ${it.source}")
            }

            if (sourceFile.isDirectory) {
                throw PipelineException("Custom attachment file is a directory: ${it.source}")
            }

            treeBuilder.attach(
                TreeAttachment.FileAttachment(
                    it.destination,
                    File(projectDir, it.source),
                    true
                )
            )

            logger.done("Registering custom attachment: ${it.source} -> ${it.destination}")
        }

        val renderedPages = mutableListOf<RenderedPage>()

        // Parse, Process, and Render All Pages
        structure.pageFiles.forEachIndexed { index, file ->
            val relativePath = file.relativeTo(projectDir).path

            logger.working("Compiling page $relativePath (${index + 1}/${structure.pageFiles.size})")
            val parsedPage = Parser.parsePage(file)
            val page = processor.process(parsedPage)
            val output = render(page)
            logger.done("Compiled $relativePath")

            renderedPages.add(
                RenderedPage(
                    page,
                    output,
                    page.projectMetadata,
                    projectDir,
                    file
                )
            )
        }

        val project = Project(renderedPages, settings)

        logger.working("Packaging build outputs")
        treeBuilder.buildOutputTreeWithAttachments(renderedPages, project, outputDir, true)
        logger.done("Output files written to ${outputDir.path}")
        val endTime = System.currentTimeMillis()
        logger.done("Done in ${endTime - startTime}ms")
    }

    override fun scanProjectStructureAndParseSettings(projectDir: File): Pair<ProjectStructure, ProjectSettings> {
        val structure = projectScanner.scanProjectStructure(projectDir)
        val parsedSettings = Parser.parseProjectSettings(structure.settingsFile)
        val settings = processor.processProjectSettings(parsedSettings.rootBlock as DirectiveNode)
        return structure to settings
    }

    override fun render(page: Page): String {
        return render(page.projectMetadata, page.root)
    }

    override fun render(projectMetadata: ProjectMetadata, root: Block): String {
        val renderer = renderer
        return renderer.postProcess(
            projectMetadata,
            """
                ${renderer.preamble(projectMetadata)}
                ${renderer.renderTitle(projectMetadata)}
                ${renderer.render(projectMetadata, root)}
                ${renderer.postamble(projectMetadata)}
            """.trimIndent()
        )
    }
}