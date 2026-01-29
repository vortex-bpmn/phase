package at.phactum.vortex.phase.pipeline.preset

import at.phactum.vortex.phase.pipeline.SimplePipeline
import at.phactum.vortex.phase.renderer.HtmlRenderer
import at.phactum.vortex.phase.api.base.TreeAttachment
import at.phactum.vortex.phase.treebuilder.HtmlTreeBuilder

class VortexHtmlPipeline : SimplePipeline(
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