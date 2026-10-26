package hexlet.code.condition;

import hexlet.code.Condition;
import hexlet.code.SqlWriter;
import java.util.List;

/** Хотя бы одно вложенное условие истинно. Вложенные условия печатаются без скобок. */
public final class Or implements Condition {
    private final List<Condition> conditions;

    public Or(List<Condition> conditions) {
        this.conditions = List.copyOf(conditions);
    }

    @Override
    public void render(SqlWriter writer) {
        for (var i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                writer.sql(" OR ");
            }
            conditions.get(i).render(writer);
        }
    }
}
