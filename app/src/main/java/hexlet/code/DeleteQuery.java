package hexlet.code;

/** Неизменяемое удаление. Без условия строится только после явного {@link #all()}. */
public final class DeleteQuery {
    private final QueryContext context;
    private final Condition where;
    private final boolean allRows;

    DeleteQuery(QueryContext context) {
        this(context, null, false);
    }

    private DeleteQuery(QueryContext context, Condition where, boolean allRows) {
        this.context = context;
        this.where = where;
        this.allRows = allRows;
    }

    /** Присоединяет условие к уже заданному через AND. */
    public DeleteQuery where(Condition condition) {
        return new DeleteQuery(context, Conditions.append(where, condition), allRows);
    }

    /** Разрешает удалить все строки таблицы. */
    public DeleteQuery all() {
        return new DeleteQuery(context, where, true);
    }

    public CompiledQuery toSql() {
        if (where == null && !allRows) {
            throw new MissingConditionException(
                    "DELETE without where(), call all() to delete every row");
        }
        var writer = context.newWriter();
        context.dialect().renderDelete(this, writer);
        return writer.toCompiledQuery();
    }

    Condition whereCondition() {
        return where;
    }
}
