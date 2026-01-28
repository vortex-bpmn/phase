package at.phactum.vortex.phase.pipeline.preset

import at.phactum.vortex.phase.pipeline.Pipeline
import at.phactum.vortex.phase.renderer.impl.HtmlRenderer
import at.phactum.vortex.phase.treebuilder.TreeAttachment
import at.phactum.vortex.phase.treebuilder.impl.HtmlTreeBuilder

class VortexHtmlPipeline : Pipeline(
    HtmlRenderer(),
    HtmlTreeBuilder().apply {
        attach(
            TreeAttachment.ResourceAttachment(
                "index.html",
                "/vortex-html/index.html"
            )
        )
        attach(
            TreeAttachment.ResourceAttachment(
                "style.css",
                "/vortex-html/style.css"
            )
        )
        attach(
            TreeAttachment.ResourceAttachment(
                "navigation.js",
                "/vortex-html/navigation.js"
            )
        )
        attach(
            TreeAttachment.ResourceAttachment(
                "open-color.css",
                "/vortex-html/open-color.css"
            )
        )
    })