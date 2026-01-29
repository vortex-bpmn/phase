package at.phactum.vortex.phase.api.contract

import at.phactum.vortex.phase.api.DirectiveType
import at.phactum.vortex.phase.api.model.*

interface AstProcessor {
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