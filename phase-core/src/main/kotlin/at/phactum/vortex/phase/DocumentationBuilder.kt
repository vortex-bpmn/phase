package at.phactum.vortex.phase

import at.phactum.vortex.phase.model.Block
import at.phactum.vortex.phase.model.Metadata
import at.phactum.vortex.phase.model.Page
import at.phactum.vortex.phase.renderer.Renderer

class DocumentationBuilder {
    private val renderers = mutableSetOf<Renderer>()

    fun registerRenderer(renderer: Renderer) {
        renderers.add(renderer)
    }

    fun render(page: Page): Map<Renderer, String> {
        return render(page.metadata, page.root)
    }

    fun render(metadata: Metadata, root: Block): Map<Renderer, String> {
        val results = mutableMapOf<Renderer, String>()
        renderers.forEach { renderer ->
            results[renderer] =
                renderer.postProcess(
                    metadata,
                    """
                    ${renderer.preamble(metadata)}
                    ${renderer.renderTitle(metadata)}
                    ${renderer.render(metadata, root)}
                    ${renderer.postamble(metadata)}
                """.trimIndent()
                )
        }
        return results
    }

    // TODO Build
}