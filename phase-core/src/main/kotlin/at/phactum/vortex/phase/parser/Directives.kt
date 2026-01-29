package at.phactum.vortex.phase.parser

import at.phactum.vortex.phase.exception.SyntaxException
import java.io.File


enum class DirectiveType(
    val identifier: String,
    val isCompound: Boolean,
    val hasValue: Boolean,
    val availableIn: ParsingContextType? = ParsingContextType.PAGE_FILE
) {
    BLOCK("block", true, false),
    END("end", false, false, null),

    META("meta", true, false),
    TITLE("title", false, true),
    AUTHOR("author", false, true),
    VERSION("version", false, true),

    SECTION("section", true, true),

    TABLE("table", true, false),
    TABLE_ROW("row", true, false),
    TABLE_COLUMN("col", true, false),
    TABLE_INLINE_COLUMN("icol", false, true),

    PROJECT_NAME("project", false, true, ParsingContextType.PROJECT_FILE),
    ATTACHMENT("attachment", true, false, ParsingContextType.PROJECT_FILE),
    ATTACHMENT_SOURCE("source", false, true, ParsingContextType.PROJECT_FILE),
    ATTACHMENT_DESTINATION("destination", false, true, ParsingContextType.PROJECT_FILE);
}
