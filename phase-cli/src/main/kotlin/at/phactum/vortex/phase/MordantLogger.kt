package at.phactum.vortex.phase

import at.phactum.vortex.phase.api.contract.Logger
import at.phactum.vortex.phase.api.model.LogStatus
import at.phactum.vortex.phase.api.model.LogStatus.*
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.Companion.gray
import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal

data class Log(
    val id: String,
    val message: String,
    var status: LogStatus
)

data class LogStyle(
    val tagStyle: TextStyle,
    val textStyle: TextStyle
)

class MordantLogger : Logger {
    private val terminal = Terminal()
    private val logs = mutableListOf<Log>()
    private var logsPrinted = 0

    private fun statusStyle(status: LogStatus): LogStyle {
        return when (status) {
            DONE  -> LogStyle(rgb("#7EDC9A"), TextColors.white)
            WORK  -> LogStyle(rgb("#82B1FF"), TextColors.gray)
            ERROR -> LogStyle(rgb("#FF8A8A"), TextColors.white)
            WARN  -> LogStyle(rgb("#F6C177"), TextColors.white) // muted peach
            INFO  -> LogStyle(rgb("#D0D0D0"), TextColors.white)
        }
    }

    private fun prepareMessage(log: Log): String {
        val maxLevelNameLength = LogStatus.entries.map { it.name.length }.maxOrNull() ?: 0
        val logLevelName = log.status.name.padEnd(maxLevelNameLength)

        val style = statusStyle(log.status)

        return (TextStyles.bold + style.tagStyle)(logLevelName) + " " + style.textStyle(log.message)
    }

    private fun render(index: Int, isNew: Boolean) {
        val log = logs[index]

        val message = prepareMessage(log)

        if (isNew) {
            terminal.println(message)
            logsPrinted++
        } else {
            val linesUp = logsPrinted - index
            terminal.cursor.move {
                up(linesUp)
                startOfLine()
                clearLine()
            }

            terminal.print(message)

            terminal.cursor.move {
                down(linesUp)
                startOfLine()
                clearLine()
            }
        }
    }

    override fun log(status: LogStatus, message: String, id: String): String {
        logs.add(Log(id, message, status))
        render(logs.size - 1, true)
        return id
    }

    override fun working(message: String, id: String): String = log(LogStatus.WORK, message, id)
    override fun error(message: String, id: String): String = log(LogStatus.ERROR, message, id)
    override fun warn(message: String, id: String): String = log(LogStatus.WARN, message, id)
    override fun info(message: String, id: String): String = log(LogStatus.INFO, message, id)

    override fun update(id: String, status: LogStatus) {
        val logIndex = logs.indexOfFirst { it.id == id }
        if (logIndex == -1) return
        logs[logIndex].status = status
        render(logIndex, false)
    }

    override fun resolve(id: String) {
        val log = logs.find { it.id == id } ?: return
        update(id, log.status.resolve())
    }
}