package at.phactum.vortex.phase

import at.phactum.vortex.phase.api.contract.Logger
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.terminal.Terminal
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MordantLogger : Logger {
    private val terminal = Terminal()

    enum class LogLevel(val tagStyle: TextStyle, val textStyle: TextStyle) {
        WORK(rgb("#3498db"), TextColors.gray(0.7)),
        DONE(rgb("#2ecc71"), TextColors.gray(0.7)),
        INFO(TextColors.gray(0.7), TextColors.gray(0.7)),
        ERR(rgb("#e74c3c"), TextColors.gray(1)),
        WARN(rgb("#f1c40f"), TextColors.gray(1))
    }

    private fun render(level: LogLevel, message: String) {
        val logLevelName = level.name.padEnd(LogLevel.entries.map { it.name.length }.max())
        val timeStamp = LocalTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss:SSS")
        )

        terminal.println(
            level.tagStyle(logLevelName) + " " +
                    TextColors.gray(0.5)(timeStamp) + " " +
                    level.textStyle(message)
        )
    }

    override fun working(message: String) {
        render(LogLevel.WORK, message)
    }

    override fun done(message: String) {
        render(LogLevel.DONE, message)
    }

    override fun info(message: String) {
        render(LogLevel.INFO, message)
    }

    override fun warning(message: String) {
        render(LogLevel.WARN, message)
    }

    override fun error(message: String) {
        render(LogLevel.ERR, message)
    }
}