package hexlet.code;

/**
 * Стратегия печати запросов для конкретной базы данных. Запросы хранят только данные, а в текст их
 * превращает диалект.
 */
public interface Dialect {
    /** Имя таблицы или колонки в кавычках этой базы. */
    String quote(String identifier);

    /** Плейсхолдер параметра с номером {@code position}, считая с единицы. */
    String placeholder(int position);

    void renderSelect(SelectQuery query, SqlWriter writer);

    void renderInsert(InsertQuery query, SqlWriter writer);

    void renderUpdate(UpdateQuery query, SqlWriter writer);

    void renderDelete(DeleteQuery query, SqlWriter writer);
}
