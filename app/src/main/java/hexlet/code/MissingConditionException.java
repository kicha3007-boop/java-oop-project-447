package hexlet.code;

/**
 * UPDATE или DELETE без условия и без явного {@code all()}. Скорее всего, условие забыто, а такой
 * запрос затронул бы всю таблицу.
 */
public class MissingConditionException extends QueryException {
    public MissingConditionException(String message) {
        super(message);
    }
}
