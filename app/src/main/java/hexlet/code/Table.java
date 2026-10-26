package hexlet.code;

/** Таблица, от которой строятся запросы. */
public final class Table {
    private final QueryContext context;

    Table(QueryContext context) {
        this.context = context;
    }

    public SelectQuery select(String... columns) {
        return new SelectQuery(context, Names.copyOf(columns));
    }

    public InsertQuery insert() {
        return new InsertQuery(context);
    }

    public UpdateQuery update() {
        return new UpdateQuery(context);
    }

    public DeleteQuery delete() {
        return new DeleteQuery(context);
    }
}
