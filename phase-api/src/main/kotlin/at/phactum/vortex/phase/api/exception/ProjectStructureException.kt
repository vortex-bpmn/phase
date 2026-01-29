package at.phactum.vortex.phase.api.exception

import java.io.File

class ProjectStructureException(override val message: String, val projectDirectory: File) : PhaseException(message) {
    override fun formattedMessage(): String {
        return "$message -- Failed in project ${projectDirectory.path}"
    }
}