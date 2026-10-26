package hexlet.code;

import java.util.List;
import java.util.Map;

/**
 * Стандартный SQL: имена в двойных кавычках, плейсхолдер {@code ?}, upsert не поддерживается.
 *
 * <p>Раскладка запросов общая для всех диалектов и защищена {@code final}. Подклассы меняют только
 * отдельные шаги: знак кавычки, плейсхолдер и печать upsert.
 */
public class BaseDialect implements Dialect {

    @Override
    public final String quote(String identifier) {
        var mark = quoteMark();
        return mark + identifier.replace(mark, mark + mark) + mark;
    }

    @Override
    public final String placeholder(int position) {
        return placeholderFor(position);
    }

    @Override
    public final void renderSelect(SelectQuery query, SqlWriter writer) {
        writer.sql("SELECT ");
        if (query.columns().isEmpty()) {
            writer.sql("*");
        } else {
            columnList(query.columns(), writer);
        }
        writer.sql(" FROM ").table();
        where(query.whereCondition(), writer);
        if (!query.orderByColumns().isEmpty()) {
            writer.sql(" ORDER BY ");
            columnList(query.orderByColumns(), writer);
        }
        if (query.limitCount() != null) {
            writer.sql(" LIMIT " + query.limitCount());
        }
        if (query.offsetCount() != null) {
            writer.sql(" OFFSET " + query.offsetCount());
        }
    }

    @Override
    public final void renderInsert(InsertQuery query, SqlWriter writer) {
        var values = query.values();
        writer.sql("INSERT INTO ").table().sql(" (");
        columnList(List.copyOf(values.keySet()), writer);
        writer.sql(") VALUES (");
        var first = true;
        for (var value : values.values()) {
            if (!first) {
                writer.sql(", ");
            }
            writer.param(value);
            first = false;
        }
        writer.sql(")");
        if (query.isUpsert()) {
            query.conflictColumns().forEach(writer::requireColumn);
            query.updateColumns().forEach(writer::requireColumn);
            renderUpsert(query.conflictColumns(), query.updateColumns(), writer);
        }
    }

    @Override
    public final void renderUpdate(UpdateQuery query, SqlWriter writer) {
        writer.sql("UPDATE ").table().sql(" SET ");
        var first = true;
        for (Map.Entry<String, Object> assignment : query.assignments().entrySet()) {
            if (!first) {
                writer.sql(", ");
            }
            writer.column(assignment.getKey()).sql(" = ").param(assignment.getValue());
            first = false;
        }
        where(query.whereCondition(), writer);
    }

    @Override
    public final void renderDelete(DeleteQuery query, SqlWriter writer) {
        writer.sql("DELETE FROM ").table();
        where(query.whereCondition(), writer);
    }

    /** Знак, которым имена берутся в кавычки. */
    protected String quoteMark() {
        return "\"";
    }

    /** Плейсхолдер параметра с номером {@code position}, считая с единицы. */
    protected String placeholderFor(int position) {
        return "?";
    }

    /**
     * Печатает хвост upsert после {@code VALUES (...)}. Колонки уже проверены по схеме. В стандарте
     * SQL upsert не определён, поэтому здесь он отклоняется.
     */
    protected void renderUpsert(
            List<String> conflictColumns, List<String> updateColumns, SqlWriter writer) {
        throw new QueryException("Upsert is not part of standard SQL, use a concrete dialect");
    }

    /** Печатает колонки через запятую: {@code "id", "email"}. */
    protected final void columnList(List<String> columns, SqlWriter writer) {
        for (var i = 0; i < columns.size(); i++) {
            if (i > 0) {
                writer.sql(", ");
            }
            writer.column(columns.get(i));
        }
    }

    /** Печатает присваивания из источника: {@code "name" = EXCLUDED."name", "age" = ...}. */
    protected final void assignmentsFrom(String source, List<String> columns, SqlWriter writer) {
        for (var i = 0; i < columns.size(); i++) {
            if (i > 0) {
                writer.sql(", ");
            }
            var column = columns.get(i);
            writer.column(column).sql(" = " + source + ".").column(column);
        }
    }

    private void where(Condition condition, SqlWriter writer) {
        if (condition != null) {
            writer.sql(" WHERE ");
            condition.render(writer);
        }
    }
}
