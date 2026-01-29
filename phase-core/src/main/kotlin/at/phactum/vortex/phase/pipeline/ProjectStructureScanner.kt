package at.phactum.vortex.phase.pipeline

import at.phactum.vortex.phase.api.Constants.PHASE_PAGE_EXTENSION
import at.phactum.vortex.phase.api.Constants.PHASE_PROJECT_EXTENSION
import at.phactum.vortex.phase.api.exception.ProjectStructureException
import org.slf4j.LoggerFactory
import java.io.File

data class ProjectStructure(
    val pageFiles: List<File>,
    val settingsFile: File
)

class ProjectStructureScanner(val projectDirectory: File) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun scanProjectStructure(): ProjectStructure {
        if (!projectDirectory.exists()) {
            throw ProjectStructureException("Project directory does not exist", projectDirectory)
        }

        if (!projectDirectory.isDirectory) {
            throw ProjectStructureException("Project root is not a directory", projectDirectory)
        }

        val pageFiles = mutableListOf<File>()
        var projectFile: File? = null

        projectDirectory.walkTopDown().forEach { file ->
            if (file.isDirectory)
                return@forEach

            if (file.extension == PHASE_PAGE_EXTENSION) {
                pageFiles.add(file)
                return@forEach
            }

            if (file.extension == PHASE_PROJECT_EXTENSION) {
                if (projectFile != null) {
                    throw ProjectStructureException("Project has a duplicate project file: ${file.path}. Previously detected ${projectFile.path}", projectDirectory)
                }

                projectFile = file
                return@forEach
            }

            log.debug("Ignoring file ${file.path} in project tree")
        }

        if (pageFiles.isEmpty()) {
            throw ProjectStructureException("Project tree does not have any page files (*.${PHASE_PAGE_EXTENSION})", projectDirectory)
        }

        if (projectFile == null) {
            throw ProjectStructureException("Project tree does not contain a project file (*.${PHASE_PROJECT_EXTENSION})", projectDirectory)
        }

        return ProjectStructure(pageFiles, projectFile)
    }

}