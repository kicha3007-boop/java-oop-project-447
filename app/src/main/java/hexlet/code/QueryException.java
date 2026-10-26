package hexlet.code;

/** Ошибка в построении запроса: неизвестное имя, неверное значение, неполный запрос. */
public class QueryException extends RuntimeException {
    public QueryException(String message) {
        super(message);
    }
}
