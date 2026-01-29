package at.phactum.vortex.phase.api.exception

abstract class PhaseException(override val message: String) : RuntimeException(message) {
    abstract fun formattedMessage(): String
}