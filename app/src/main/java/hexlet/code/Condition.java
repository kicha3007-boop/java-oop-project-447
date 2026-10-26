package hexlet.code;

/** Условие запроса. Печатает себя само через публичные методы {@link SqlWriter}. */
@FunctionalInterface
public interface Condition {
    void render(SqlWriter writer);
}
