package hexlet.code;

/** Общее для всех запросов к одной таблице: диалект, схема и имя таблицы. */
record QueryContext(Dialect dialect, Schema schema, String table) {
    SqlWriter newWriter() {
        return new SqlWriter(dialect, schema, table);
    }
}
