package hexlet.code;

import java.util.ArrayList;
import java.util.List;

/** Неизменяемая выборка. Каждый метод возвращает новую выборку, исходная не меняется. */
public final class SelectQuery {
    private final QueryContext context;
    private final List<String> columns;
    private final Condition where;
    private final List<String> orderBy;
    private final Integer limit;
    private final Integer offset;

    SelectQuery(QueryContext context, List<String> columns) {
        this(context, columns, null, List.of(), null, null);
    }

    private SelectQuery(
            QueryContext context,
            List<String> columns,
            Condition where,
            List<String> orderBy,
            Integer limit,
            Integer offset) {
        this.context = context;
        this.columns = List.copyOf(columns);
        this.where = where;
        this.orderBy = List.copyOf(orderBy);
        this.limit = limit;
        this.offset = offset;
    }

    /** Присоединяет условие к уже заданному через AND. */
    public SelectQuery where(Condition condition) {
        return new SelectQuery(
                context, columns, Conditions.append(where, condition), orderBy, limit, offset);
    }

    /** Добавляет колонку к сортировке. */
    public SelectQuery orderBy(String column) {
        var newOrderBy = new ArrayList<>(orderBy);
        newOrderBy.addAll(Names.copyOf(column));
        return new SelectQuery(context, columns, where, newOrderBy, limit, offset);
    }

    /** Заменяет число строк в результате. */
    public SelectQuery limit(int count) {
        return new SelectQuery(context, columns, where, orderBy, count, offset);
    }

    /** Заменяет число пропускаемых строк. Работает только вместе с {@link #limit(int)}. */
    public SelectQuery offset(int count) {
        return new SelectQuery(context, columns, where, orderBy, limit, count);
    }

    public CompiledQuery toSql() {
        if (limit != null && limit < 0) {
            throw new QueryException("LIMIT must not be negative: " + limit);
        }
        if (offset != null && offset < 0) {
            throw new QueryException("OFFSET must not be negative: " + offset);
        }
        if (offset != null && limit == null) {
            throw new QueryException("OFFSET requires LIMIT");
        }
        var writer = context.newWriter();
        context.dialect().renderSelect(this, writer);
        return writer.toCompiledQuery();
    }

    List<String> columns() {
        return columns;
    }

    Condition whereCondition() {
        return where;
    }

    List<String> orderByColumns() {
        return orderBy;
    }

    Integer limitCount() {
        return limit;
    }

    Integer offsetCount() {
        return offset;
    }
}
