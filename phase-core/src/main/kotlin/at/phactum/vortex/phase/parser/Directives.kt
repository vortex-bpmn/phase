package at.phactum.vortex.phase.parser

import at.phactum.vortex.phase.exception.SyntaxException
import java.io.File

enum class DirectiveType(val isCompound: Boolean, val hasValue: Boolean) {
    BLOCK(true, false),
    END(false, false),

    META(true, false),
    TITLE(false, true),
    AUTHOR(false, true),
    VERSION(false, true),

    SECTION(true, true),

    TABLE(true, false),
    ROW(true, false),
    COL(true, false),
    ICOL(false, true);

    companion object {
        fun parse(s: String, file: File, lineNumber: Int, columnNumber: Int): DirectiveType {
            return entries.find { d -> d.name.equals(s, ignoreCase = true) }
                ?: throw SyntaxException("Unknown directive \"$s\"", file, lineNumber, columnNumber)
        }
    }
}
