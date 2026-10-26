package hexlet.code;

import java.util.List;

/** Проверка списков имён колонок, которые приходят в методы запросов. */
final class Names {
    private Names() {}

    static List<String> copyOf(String... names) {
        if (names == null) {
            throw new QueryException("Column list must not be null");
        }
        for (var name : names) {
            if (name == null) {
                throw new QueryException("Column name must not be null");
            }
        }
        return List.of(names);
    }
}
