package hexlet.code.condition;

import hexlet.code.Condition;
import hexlet.code.QueryException;
import hexlet.code.SqlWriter;

/** Отрицание условия. Вложенное условие всегда в скобках: {@code NOT ("id" = $1)}. */
public final class Not implements Condition {
    private final Condition condition;

    public Not(Condition condition) {
        if (condition == null) {
            throw new QueryException("Condition must not be null");
        }
        this.condition = condition;
    }

    @Override
    public void render(SqlWriter writer) {
        writer.sql("NOT (");
        condition.render(writer);
        writer.sql(")");
    }
}
