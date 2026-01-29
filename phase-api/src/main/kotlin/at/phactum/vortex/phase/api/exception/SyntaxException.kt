package at.phactum.vortex.phase.api.exception

import java.io.File

class SyntaxException(override val message: String, val file: File, val line: Int, val column: Int) : PhaseException(message) {
    override fun formattedMessage(): String {
        return "$message -- Failed at ($line:$column) in file \"${file.path}\""
    }
}