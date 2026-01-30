package at.phactum.vortex.phase.pipeline

import at.phactum.vortex.phase.api.Constants.PHASE_PAGE_EXTENSION
import at.phactum.vortex.phase.api.Constants.PHASE_PROJECT_EXTENSION
import at.phactum.vortex.phase.api.base.ProjectScanner
import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.api.exception.ProjectStructureException
import at.phactum.vortex.phase.api.model.ProjectStructure
import java.io.File

class StandardProjectScanner(override val logger: Logger) : ProjectScanner(logger) {

    override fun scanProjectStructure(projectDir: File): ProjectStructure {
        if (!projectDir.exists()) {
            throw ProjectStructureException("Project directory does not exist", projectDir)
        }

        if (!projectDir.isDirectory) {
            throw ProjectStructureException("Project directory is not a directory", projectDir)
        }

        val pageFiles = mutableListOf<File>()
        var projectFile: File? = null

        projectDir.walkTopDown().forEach { file ->
            if (file.isDirectory)
                return@forEach

            if (file.extension == PHASE_PAGE_EXTENSION) {
                pageFiles.add(file)
                return@forEach
            }

            if (file.extension == PHASE_PROJECT_EXTENSION) {
                if (projectFile != null) {
                    throw ProjectStructureException(
                        "Project has a duplicate project file: ${file.path}. Previously detected ${projectFile.path}",
                        projectDir
                    )
                }

                projectFile = file
                return@forEach
            }

            logger.warning("Ignoring unexpected file in project directory: ${file.path}")
        }

        if (pageFiles.isEmpty()) {
            throw ProjectStructureException(
                "Project tree does not have any page files (*.${PHASE_PAGE_EXTENSION})",
                projectDir
            )
        }

        if (projectFile == null) {
            throw ProjectStructureException(
                "Project tree does not contain a project file (*.${PHASE_PROJECT_EXTENSION})",
                projectDir
            )
        }

        return ProjectStructure(projectDir, pageFiles, projectFile)
    }

}