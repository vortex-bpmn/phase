package at.phactum.vortex.phase.api.contract

interface Logger {
    fun working(message: String)
    fun done(message: String)
    fun info(message: String)
    fun warning(message: String)
    fun error(message: String)
}