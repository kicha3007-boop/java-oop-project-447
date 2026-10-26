package hexlet.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Напечатанный запрос: текст с плейсхолдерами и значения к ним по порядку.
 *
 * @param sql текст запроса
 * @param params значения параметров, среди них может быть {@code null}
 */
public record CompiledQuery(String sql, List<Object> params) {
    public CompiledQuery {
        Objects.requireNonNull(sql, "sql");
        params = Collections.unmodifiableList(new ArrayList<>(params));
    }
}
