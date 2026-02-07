package at.phactum.vortex.phase.api.model

enum class LogStatus(val resolvesTo: LogStatus? = null) {
    DONE,
    FAIL,
    WORK(DONE),

    ERROR,
    WARN,
    INFO;

    fun resolve(): LogStatus = resolvesTo ?: this
}
