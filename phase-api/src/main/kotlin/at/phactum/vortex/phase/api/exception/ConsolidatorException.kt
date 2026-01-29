package at.phactum.vortex.phase.api.exception

class ConsolidatorException(override val message: String) : PhaseException(message) {
    override fun formattedMessage(): String {
        return message
    }
}