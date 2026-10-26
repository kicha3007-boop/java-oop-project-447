package hexlet.code;

import java.util.List;
import java.util.Map;

/** Схема и точки входа, общие для тестов. Объекты неизменяемы, поэтому их можно делить. */
final class QueryBuilderFixtures {
    static final Schema SCHEMA =
            Schema.of(
                    Map.of(
                            "users", List.of("id", "name", "email", "age", "active"),
                            "posts", List.of("id", "title")));

    private QueryBuilderFixtures() {}

    static Sql postgres() {
        return Sql.using(new PostgresDialect(), SCHEMA);
    }

    static Sql mysql() {
        return Sql.using(new MySqlDialect(), SCHEMA);
    }

    static Table users() {
        return postgres().table("users");
    }
}
