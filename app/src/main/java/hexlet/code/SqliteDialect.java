package hexlet.code;

import java.util.List;

/** SQLite: кавычки и плейсхолдер стандартные, upsert как в PostgreSQL. */
public class SqliteDialect extends BaseDialect {

    @Override
    protected void renderUpsert(
            List<String> conflictColumns, List<String> updateColumns, SqlWriter writer) {
        writer.sql(" ON CONFLICT (");
        columnList(conflictColumns, writer);
        writer.sql(") DO UPDATE SET ");
        assignmentsFrom("EXCLUDED", updateColumns, writer);
    }
}
