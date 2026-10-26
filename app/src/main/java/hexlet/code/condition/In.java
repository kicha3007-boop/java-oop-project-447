package hexlet.code.condition;

import hexlet.code.Condition;
import hexlet.code.QueryException;
import hexlet.code.SqlWriter;
import java.util.List;

/** Значение колонки входит в список: {@code "id" IN ($1, $2)}. */
public final class In implements Condition {
    private final String column;
    private final List<Object> values;

    public In(String column, Object... values) {
        if (column == null) {
            throw new QueryException("Column must not be null");
        }
        if (values == null || values.length == 0) {
            throw new QueryException("IN requires at least one value for column " + column);
        }
        for (var value : values) {
            if (value == null) {
                throw new QueryException("IN must not contain null for column " + column);
            }
        }
        this.column = column;
        this.values = List.of(values);
    }

    @Override
    public void render(SqlWriter writer) {
        writer.column(column).sql(" IN (");
        for (var i = 0; i < values.size(); i++) {
            if (i > 0) {
                writer.sql(", ");
            }
            writer.param(values.get(i));
        }
        writer.sql(")");
    }
}
