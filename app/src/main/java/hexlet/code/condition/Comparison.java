package hexlet.code.condition;

import hexlet.code.Condition;
import hexlet.code.QueryException;
import hexlet.code.SqlWriter;

/** Сравнение колонки со значением: {@code "age" > $1}. */
public final class Comparison implements Condition {
    private final String column;
    private final String operator;
    private final Object value;

    public Comparison(String column, String operator, Object value) {
        if (column == null) {
            throw new QueryException("Column must not be null");
        }
        if (value == null) {
            throw new QueryException(
                    "Comparison with null is never true, use isNull() for column " + column);
        }
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public void render(SqlWriter writer) {
        writer.column(column).sql(" " + operator + " ").param(value);
    }
}
