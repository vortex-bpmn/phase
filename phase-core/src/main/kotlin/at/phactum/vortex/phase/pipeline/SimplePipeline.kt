package at.phactum.vortex.phase.pipeline

import at.phactum.vortex.phase.api.exception.PipelineException
import at.phactum.vortex.phase.api.model.Block
import at.phactum.vortex.phase.api.model.Metadata
import at.phactum.vortex.phase.api.model.Page
import at.phactum.vortex.phase.api.model.Project
import at.phactum.vortex.phase.api.model.RenderedPage
import at.phactum.vortex.phase.api.model.DirectiveNode
import at.phactum.vortex.phase.parser.Parser
import at.phactum.vortex.phase.processor.Processor
import at.phactum.vortex.phase.api.contract.Renderer
import at.phactum.vortex.phase.api.base.TreeAttachment
import at.phactum.vortex.phase.api.base.TreeBuilder
import at.phactum.vortex.phase.api.contract.Pipeline
import org.slf4j.LoggerFactory
import java.io.File

open class SimplePipeline(val renderer: Renderer, val treeBuilder: TreeBuilder): Pipeline {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun buildProject(root: File, outputDir: File) {
        val startTime = System.currentTimeMillis()
        val structure = ProjectStructureScanner(root).scanProjectStructure()

        val processor = Processor()

        val parsedSettings = Parser.parseProjectSettings(structure.settingsFile)
        val settings = processor.processProjectSettings(parsedSettings.rootBlock as DirectiveNode)

        log.info("Building project \"${settings.name}\" at ${root.path}")

        settings.attachments.forEach {
            val sourceFile = File(root, it.source)

            if (!sourceFile.exists()) {
                throw PipelineException("Custom attachment file does not exist: ${it.source}")
            }

            if (sourceFile.isDirectory) {
                throw PipelineException("Custom attachment file is a directory: ${it.source}")
            }

            treeBuilder.attach(TreeAttachment.FileAttachment(
                it.destination,
                File(root, it.source),
                true
            ))

            log.info("Registering custom attachment: ${it.source} -> ${it.destination}")
        }

        val renderedPages = mutableListOf<RenderedPage>()

        // Parse, Process, and Render All Pages
        structure.pageFiles.forEach { file ->
            val relativePath = file.relativeTo(root).path

            log.info("Compiling page $relativePath")
            val parsedPage = Parser.parsePage(file)
            val page = processor.process(parsedPage)
            val output = render(page)

            renderedPages.add(
                RenderedPage(
                    page,
                    output,
                    page.metadata,
                    root,
                    file
                )
            )
        }

        val project = Project(renderedPages, settings)

        log.info("Building output files")
        treeBuilder.buildOutputTreeWithAttachments(renderedPages, project, outputDir, true)
        log.info("Output files written to ${outputDir.path}")
        val endTime = System.currentTimeMillis()
        log.info("Done in ${endTime - startTime}ms")
    }

    override fun render(page: Page): String {
        return render(page.metadata, page.root)
    }

    override fun render(metadata: Metadata, root: Block): String {
        val renderer = renderer
        return renderer.postProcess(
            metadata,
            """
                ${renderer.preamble(metadata)}
                ${renderer.renderTitle(metadata)}
                ${renderer.render(metadata, root)}
                ${renderer.postamble(metadata)}
            """.trimIndent()
        )
    }
}