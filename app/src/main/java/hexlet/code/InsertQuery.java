package hexlet.code;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Неизменяемая вставка одной строки, при необходимости с upsert. */
public final class InsertQuery {
    private final QueryContext context;
    private final Map<String, Object> values;
    private final List<String> conflictColumns;
    private final List<String> updateColumns;

    InsertQuery(QueryContext context) {
        this(context, Map.of(), List.of(), List.of());
    }

    private InsertQuery(
            QueryContext context,
            Map<String, Object> values,
            List<String> conflictColumns,
            List<String> updateColumns) {
        this.context = context;
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
        this.conflictColumns = List.copyOf(conflictColumns);
        this.updateColumns = List.copyOf(updateColumns);
    }

    /** Добавляет колонку со значением. Значение может быть {@code null}. */
    public InsertQuery value(String column, Object value) {
        var newValues = new LinkedHashMap<>(values);
        newValues.put(Names.copyOf(column).getFirst(), value);
        return new InsertQuery(context, newValues, conflictColumns, updateColumns);
    }

    /** Колонки уникального ключа, по которому определяется конфликт. */
    public InsertQuery onConflict(String... columns) {
        return new InsertQuery(context, values, Names.copyOf(columns), updateColumns);
    }

    /** Колонки, которые при конфликте обновятся значениями из вставки. */
    public InsertQuery doUpdate(String... columns) {
        return new InsertQuery(context, values, conflictColumns, Names.copyOf(columns));
    }

    public CompiledQuery toSql() {
        if (values.isEmpty()) {
            throw new QueryException("INSERT without values");
        }
        if (conflictColumns.isEmpty() && !updateColumns.isEmpty()) {
            throw new QueryException("doUpdate() requires onConflict()");
        }
        if (!conflictColumns.isEmpty() && updateColumns.isEmpty()) {
            throw new QueryException("onConflict() requires doUpdate()");
        }
        var writer = context.newWriter();
        context.dialect().renderInsert(this, writer);
        return writer.toCompiledQuery();
    }

    Map<String, Object> values() {
        return values;
    }

    boolean isUpsert() {
        return !conflictColumns.isEmpty();
    }

    List<String> conflictColumns() {
        return conflictColumns;
    }

    List<String> updateColumns() {
        return updateColumns;
    }
}
