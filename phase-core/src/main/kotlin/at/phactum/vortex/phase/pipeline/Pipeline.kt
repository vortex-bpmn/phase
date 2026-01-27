package at.phactum.vortex.phase.pipeline

import at.phactum.vortex.phase.consolidator.TreeBuilder
import at.phactum.vortex.phase.RenderedPage
import at.phactum.vortex.phase.exception.BuilderException
import at.phactum.vortex.phase.model.Block
import at.phactum.vortex.phase.model.Metadata
import at.phactum.vortex.phase.model.Page
import at.phactum.vortex.phase.parser.Parser
import at.phactum.vortex.phase.parser.processor.Processor
import at.phactum.vortex.phase.renderer.Renderer
import org.slf4j.LoggerFactory
import java.io.File

class Pipeline(val renderer: Renderer, val treeBuilder: TreeBuilder) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun buildProject(root: File, outputDirectory: File) {
        if (!root.exists())
            throw BuilderException("Project directory does not exist: ${root.path}")

        if (!root.isDirectory)
            throw BuilderException("Project root must be a directory: ${root.path}")

        val renderedPages = mutableListOf<RenderedPage>()

        val startTime = System.currentTimeMillis()
        log.info("Building project ${root.path}")

        // Render all pages
        root.walkTopDown().forEach { file ->
            if (file.isDirectory)
                return@forEach

            val relativeFile = file.relativeTo(root)
            val relativePath = relativeFile.path

            log.info("Compiling $relativePath")
            val parsedPage = Parser(file.readText(), file).parse()
            val page = Processor().process(parsedPage)
            val output = render(page)

            renderedPages.add(
                RenderedPage(
                    output,
                    page.metadata,
                    root,
                    file
                )
            )
        }

        log.info("Building output tree")
        treeBuilder.buildOutputTree(renderedPages, outputDirectory)
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