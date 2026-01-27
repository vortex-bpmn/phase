package at.phactum.vortex.phase.exception

class ComposerException(override val message: String) : PhaseException(message) {
    override fun formattedMessage(): String {
        return message
    }
}