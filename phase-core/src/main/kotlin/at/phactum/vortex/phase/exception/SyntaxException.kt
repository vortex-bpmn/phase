package at.phactum.vortex.phase.exception

class SyntaxException(override val message: String, val file: String, val line: Int, val column: Int) : PhaseException(message) {
    override fun formattedMessage(): String {
        return "$message -- Failed at ($line:$column) in file \"$file\""
    }
}