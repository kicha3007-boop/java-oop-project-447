package hexlet.code;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Неизменяемое обновление. Без условия строится только после явного {@link #all()}. */
public final class UpdateQuery {
    private final QueryContext context;
    private final Map<String, Object> assignments;
    private final Condition where;
    private final boolean allRows;

    UpdateQuery(QueryContext context) {
        this(context, Map.of(), null, false);
    }

    private UpdateQuery(
            QueryContext context,
            Map<String, Object> assignments,
            Condition where,
            boolean allRows) {
        this.context = context;
        this.assignments = Collections.unmodifiableMap(new LinkedHashMap<>(assignments));
        this.where = where;
        this.allRows = allRows;
    }

    /** Присваивает колонке значение. Значение может быть {@code null}. */
    public UpdateQuery set(String column, Object value) {
        var newAssignments = new LinkedHashMap<>(assignments);
        newAssignments.put(Names.copyOf(column).getFirst(), value);
        return new UpdateQuery(context, newAssignments, where, allRows);
    }

    /** Присоединяет условие к уже заданному через AND. */
    public UpdateQuery where(Condition condition) {
        return new UpdateQuery(context, assignments, Conditions.append(where, condition), allRows);
    }

    /** Разрешает обновить все строки таблицы. */
    public UpdateQuery all() {
        return new UpdateQuery(context, assignments, where, true);
    }

    public CompiledQuery toSql() {
        if (assignments.isEmpty()) {
            throw new QueryException("UPDATE without set()");
        }
        if (where == null && !allRows) {
            throw new MissingConditionException(
                    "UPDATE without where(), call all() to update every row");
        }
        var writer = context.newWriter();
        context.dialect().renderUpdate(this, writer);
        return writer.toCompiledQuery();
    }

    Map<String, Object> assignments() {
        return assignments;
    }

    Condition whereCondition() {
        return where;
    }
}
