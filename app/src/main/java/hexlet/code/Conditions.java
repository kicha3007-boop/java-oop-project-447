package hexlet.code;

import hexlet.code.condition.And;
import hexlet.code.condition.Comparison;
import hexlet.code.condition.In;
import hexlet.code.condition.IsNull;
import hexlet.code.condition.Not;
import hexlet.code.condition.Or;
import java.util.List;

/** Фабрика условий для {@code where()}. */
public final class Conditions {
    private Conditions() {}

    public static Condition eq(String column, Object value) {
        return new Comparison(column, "=", value);
    }

    public static Condition ne(String column, Object value) {
        return new Comparison(column, "<>", value);
    }

    public static Condition gt(String column, Object value) {
        return new Comparison(column, ">", value);
    }

    public static Condition lt(String column, Object value) {
        return new Comparison(column, "<", value);
    }

    public static Condition in(String column, Object... values) {
        return new In(column, values);
    }

    public static Condition isNull(String column) {
        return new IsNull(column);
    }

    /** Все условия истинны. Одно условие возвращается как есть. */
    public static Condition and(Condition... conditions) {
        var parts = requireConditions(conditions);
        return parts.size() == 1 ? parts.getFirst() : new And(parts);
    }

    /** Хотя бы одно условие истинно. Одно условие возвращается как есть. */
    public static Condition or(Condition... conditions) {
        var parts = requireConditions(conditions);
        return parts.size() == 1 ? parts.getFirst() : new Or(parts);
    }

    public static Condition not(Condition condition) {
        return new Not(condition);
    }

    /** Присоединяет условие к уже заданному в запросе, как повторный {@code where()}. */
    static Condition append(Condition existing, Condition added) {
        if (added == null) {
            throw new QueryException("Condition must not be null");
        }
        return existing == null ? added : and(existing, added);
    }

    private static List<Condition> requireConditions(Condition... conditions) {
        if (conditions == null || conditions.length == 0) {
            throw new QueryException("At least one condition is required");
        }
        for (var condition : conditions) {
            if (condition == null) {
                throw new QueryException("Condition must not be null");
            }
        }
        return List.of(conditions);
    }
}
