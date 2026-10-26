package hexlet.code;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Описание базы: какие таблицы есть и какие у них колонки. Все имена проверяются здесь. */
public final class Schema {
    private final Map<String, Set<String>> tables;

    private Schema(Map<String, Set<String>> tables) {
        this.tables = tables;
    }

    public static Schema of(Map<String, List<String>> tables) {
        if (tables == null) {
            throw new QueryException("Schema tables must not be null");
        }
        var copy = new HashMap<String, Set<String>>();
        tables.forEach((table, columns) -> copy.put(table, Set.copyOf(columns)));
        return new Schema(Map.copyOf(copy));
    }

    void requireTable(String table) {
        if (table == null || !tables.containsKey(table)) {
            throw new QueryException("Unknown table: " + table);
        }
    }

    void requireColumn(String table, String column) {
        requireTable(table);
        if (column == null || !tables.get(table).contains(column)) {
            throw new QueryException("Unknown column " + column + " in table " + table);
        }
    }
}
