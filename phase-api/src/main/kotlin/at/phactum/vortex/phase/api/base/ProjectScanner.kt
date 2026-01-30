package at.phactum.vortex.phase.api.base

import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.api.model.ProjectStructure
import java.io.File

abstract class ProjectScanner(open val logger: Logger) {
    abstract fun scanProjectStructure(projectDir: File): ProjectStructure
}