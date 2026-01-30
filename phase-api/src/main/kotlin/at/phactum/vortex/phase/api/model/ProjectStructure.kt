package at.phactum.vortex.phase.api.model

import java.io.File

data class ProjectStructure(
    val projectDir: File,
    val pageFiles: List<File>,
    val settingsFile: File
)
