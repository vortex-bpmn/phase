package at.phactum.vortex.phase.vortex

import at.phactum.vortex.phase.api.base.TreeAttachment
import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.pipeline.StandardPipeline
import at.phactum.vortex.phase.pipeline.StandardProjectScanner
import at.phactum.vortex.phase.processor.StandardProcessor
import at.phactum.vortex.phase.rendererimport.StandardHtmlRenderer
import at.phactum.vortex.phase.treebuilder.StandardHtmlTreeBuilder

class VortexHtmlPipeline(override val logger: Logger) : StandardPipeline(
    logger,
    StandardProjectScanner(logger),
    StandardProcessor(logger),
    StandardHtmlRenderer(logger),
    StandardHtmlTreeBuilder(logger).apply {
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
    },
)