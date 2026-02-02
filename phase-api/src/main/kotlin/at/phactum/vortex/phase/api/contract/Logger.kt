package at.phactum.vortex.phase.api.contract

import at.phactum.vortex.phase.api.model.LogStatus
import java.util.UUID

interface Logger {
    fun log(status: LogStatus, message: String, id: String = UUID.randomUUID().toString()): String

    fun working(message: String, id: String = UUID.randomUUID().toString()): String
    fun error(message: String, id: String = UUID.randomUUID().toString()): String
    fun warn(message: String, id: String = UUID.randomUUID().toString()): String
    fun info(message: String, id: String = UUID.randomUUID().toString()): String

    fun update(id: String, status: LogStatus)
    fun resolve(id: String)
}