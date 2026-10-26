package hexlet.code.condition;

import hexlet.code.Condition;
import hexlet.code.SqlWriter;
import java.util.List;

/**
 * Все вложенные условия истинны. {@code AND} связывает сильнее {@code OR}, поэтому вложенный {@link
 * Or} печатается в скобках, остальные условия без них.
 */
public final class And implements Condition {
    private final List<Condition> conditions;

    public And(List<Condition> conditions) {
        this.conditions = List.copyOf(conditions);
    }

    @Override
    public void render(SqlWriter writer) {
        for (var i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                writer.sql(" AND ");
            }
            var condition = conditions.get(i);
            if (condition instanceof Or) {
                writer.sql("(");
                condition.render(writer);
                writer.sql(")");
            } else {
                condition.render(writer);
            }
        }
    }
}
