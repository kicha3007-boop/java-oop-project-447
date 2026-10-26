package hexlet.code;

import java.util.Objects;

/** Точка входа: диалект и схема задаются один раз, дальше запросы строятся от таблиц. */
public final class Sql {
    private final Dialect dialect;
    private final Schema schema;

    private Sql(Dialect dialect, Schema schema) {
        this.dialect = dialect;
        this.schema = schema;
    }

    public static Sql using(Dialect dialect, Schema schema) {
        Objects.requireNonNull(dialect, "dialect");
        Objects.requireNonNull(schema, "schema");
        return new Sql(dialect, schema);
    }

    public Table table(String name) {
        return new Table(new QueryContext(dialect, schema, name));
    }
}
