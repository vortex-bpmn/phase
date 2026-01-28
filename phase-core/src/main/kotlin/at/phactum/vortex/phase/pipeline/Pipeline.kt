package at.phactum.vortex.phase.pipeline

import at.phactum.vortex.phase.model.*
import at.phactum.vortex.phase.parser.DirectiveNode
import at.phactum.vortex.phase.parser.Parser
import at.phactum.vortex.phase.parser.processor.Processor
import at.phactum.vortex.phase.renderer.Renderer
import at.phactum.vortex.phase.treebuilder.TreeBuilder
import org.slf4j.LoggerFactory
import java.io.File

open class Pipeline(val renderer: Renderer, val treeBuilder: TreeBuilder) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun buildProject(root: File, outputDirectory: File) {
        val startTime = System.currentTimeMillis()
        val structure = ProjectStructureScanner(root).scanProjectStructure()

        val processor = Processor()

        val parsedSettings = Parser.parseProjectSettings(structure.settingsFile)
        val settings = processor.processProjectSettings(parsedSettings.rootBlock as DirectiveNode)

        log.info("Building project \"${settings.name}\" at ${root.path}")

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
        treeBuilder.buildOutputTreeWithAttachments(renderedPages, project, outputDirectory, true)
        log.info("Output files written to ${outputDirectory.path}")
        val endTime = System.currentTimeMillis()
        log.info("Done in ${endTime - startTime}ms")
    }

    private fun render(page: Page): String {
        return render(page.metadata, page.root)
    }

    private fun render(metadata: Metadata, root: Block): String {
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