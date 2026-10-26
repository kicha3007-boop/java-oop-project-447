package hexlet.code;

import java.util.ArrayList;
import java.util.List;

/**
 * Накопитель текста запроса и его параметров. Знает диалект, схему и текущую таблицу, поэтому
 * колонки сразу проверяются по схеме, а значения уходят в список параметров.
 */
public final class SqlWriter {
    private final Dialect dialect;
    private final Schema schema;
    private final String table;
    private final StringBuilder text = new StringBuilder();
    private final List<Object> params = new ArrayList<>();

    SqlWriter(Dialect dialect, Schema schema, String table) {
        this.dialect = dialect;
        this.schema = schema;
        this.table = table;
    }

    /** Проверяет колонку по схеме и печатает её в кавычках диалекта. */
    public SqlWriter column(String name) {
        requireColumn(name);
        text.append(dialect.quote(name));
        return this;
    }

    /** Добавляет значение в параметры и печатает плейсхолдер диалекта. */
    public SqlWriter param(Object value) {
        params.add(value);
        text.append(dialect.placeholder(params.size()));
        return this;
    }

    /** Печатает текст как есть. */
    public SqlWriter sql(String sqlText) {
        text.append(sqlText);
        return this;
    }

    SqlWriter table() {
        schema.requireTable(table);
        text.append(dialect.quote(table));
        return this;
    }

    void requireColumn(String name) {
        schema.requireColumn(table, name);
    }

    CompiledQuery toCompiledQuery() {
        return new CompiledQuery(text.toString(), params);
    }
}
