package at.phactum.vortex.phase.api.base

import at.phactum.vortex.phase.api.DirectiveType
import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.api.model.DirectiveNode
import at.phactum.vortex.phase.api.model.Element
import at.phactum.vortex.phase.api.model.Node
import at.phactum.vortex.phase.api.model.Page
import at.phactum.vortex.phase.api.model.ParseResult
import at.phactum.vortex.phase.api.model.ProjectSettings
import at.phactum.vortex.phase.api.model.Schema

abstract class AstProcessor(open val logger: Logger) {
    abstract fun process(node: Node): Element
    abstract fun process(nodes: List<Node>): List<Element>
    abstract fun process(page: ParseResult.ParsedPage): Page
    abstract fun processProjectSettings(block: DirectiveNode): ProjectSettings
    abstract fun processSchema(
        block: DirectiveNode,
        uniqueFields: List<DirectiveType>,
        repeatingFields: List<DirectiveType> = mutableListOf()
    ): Schema
}