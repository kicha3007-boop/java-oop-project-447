package hexlet.code.condition;

import hexlet.code.Condition;
import hexlet.code.QueryException;
import hexlet.code.SqlWriter;

/** Колонка пуста: {@code "email" IS NULL}. */
public final class IsNull implements Condition {
    private final String column;

    public IsNull(String column) {
        if (column == null) {
            throw new QueryException("Column must not be null");
        }
        this.column = column;
    }

    @Override
    public void render(SqlWriter writer) {
        writer.column(column).sql(" IS NULL");
    }
}
