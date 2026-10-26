package hexlet.code;

import java.util.List;

/** PostgreSQL: плейсхолдеры {@code $1}, {@code $2}, upsert через {@code ON CONFLICT}. */
public class PostgresDialect extends BaseDialect {

    @Override
    protected String placeholderFor(int position) {
        return "$" + position;
    }

    @Override
    protected void renderUpsert(
            List<String> conflictColumns, List<String> updateColumns, SqlWriter writer) {
        writer.sql(" ON CONFLICT (");
        columnList(conflictColumns, writer);
        writer.sql(") DO UPDATE SET ");
        assignmentsFrom("EXCLUDED", updateColumns, writer);
    }
}
