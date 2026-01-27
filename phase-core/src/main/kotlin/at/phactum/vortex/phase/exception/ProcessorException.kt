package at.phactum.vortex.phase.exception

class ProcessorException(override val message: String, val position: Position? = null) : PhaseException(message) {
    override fun formattedMessage(): String {
        if (position == null)
            return message

        return "$message -- Failed at (${position.line}:${position.column})"
    }
}