package at.phactum.vortex.phase.api.base

import at.phactum.vortex.phase.api.DirectiveType
import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.api.model.Page
import at.phactum.vortex.phase.api.model.ProjectSettings
import at.phactum.vortex.phase.api.model.Schema
import at.phactum.vortex.phase.api.model.tree.AstNode
import at.phactum.vortex.phase.api.model.tree.DirectiveInlineValueNode
import at.phactum.vortex.phase.api.model.tree.ParseResult
import at.phactum.vortex.phase.api.model.tree.RenderNode

abstract class AstProcessor(open val logger: Logger) {
    abstract fun process(node: AstNode): RenderNode
    abstract fun process(nodes: List<AstNode>): List<RenderNode>
    abstract fun process(page: ParseResult.ParsedPage): Page
    abstract fun processProjectSettings(block: AstNode.DirectiveNode): ProjectSettings
    abstract fun processSchema(
        block: AstNode.DirectiveNode,
        uniqueFields: List<DirectiveType>,
        repeatingFields: List<DirectiveType> = mutableListOf()
    ): Schema

    abstract fun defineFunction(
        specification: DirectiveInlineValueNode.InlineFunctionSpecNode,
        block: AstNode.BlockNode
    )

    abstract fun invokeFunction(functionIdentifier: String, parameter: AstNode): AstNode
}