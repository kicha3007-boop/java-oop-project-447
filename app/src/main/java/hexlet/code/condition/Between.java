package hexlet.code.condition;

import hexlet.code.Condition;
import hexlet.code.QueryException;
import hexlet.code.SqlWriter;

/**
 * Значение колонки лежит в диапазоне, включая границы: {@code "age" BETWEEN $1 AND $2}. Добавлено
 * без правки библиотеки и печатает себя только публичными методами {@link SqlWriter}.
 */
public final class Between implements Condition {
    private final String column;
    private final Object from;
    private final Object to;

    public Between(String column, Object from, Object to) {
        if (column == null) {
            throw new QueryException("Column must not be null");
        }
        if (from == null || to == null) {
            throw new QueryException("BETWEEN bounds must not be null for column " + column);
        }
        this.column = column;
        this.from = from;
        this.to = to;
    }

    @Override
    public void render(SqlWriter writer) {
        writer.column(column).sql(" BETWEEN ").param(from).sql(" AND ").param(to);
    }
}
