package hexlet.code;

import java.util.List;

/** MySQL: имена в обратных кавычках, плейсхолдер {@code ?}, upsert через ON DUPLICATE KEY. */
public class MySqlDialect extends BaseDialect {

    @Override
    protected String quoteMark() {
        return "`";
    }

    @Override
    protected void renderUpsert(
            List<String> conflictColumns, List<String> updateColumns, SqlWriter writer) {
        writer.sql(" AS new ON DUPLICATE KEY UPDATE ");
        assignmentsFrom("new", updateColumns, writer);
    }
}
