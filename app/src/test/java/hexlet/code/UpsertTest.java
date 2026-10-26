package hexlet.code;

import static hexlet.code.QueryBuilderFixtures.SCHEMA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UpsertTest {

    private static InsertQuery insert(Dialect dialect) {
        return Sql.using(dialect, SCHEMA)
                .table("users")
                .insert()
                .value("email", "rob@example.com")
                .value("name", "rob");
    }

    static Stream<Arguments> singleColumnUpserts() {
        return Stream.of(
                Arguments.of(
                        new PostgresDialect(),
                        "INSERT INTO \"users\" (\"email\", \"name\") VALUES ($1, $2)"
                                + " ON CONFLICT (\"email\") DO UPDATE SET \"name\" = EXCLUDED.\"name\""),
                Arguments.of(
                        new MySqlDialect(),
                        "INSERT INTO `users` (`email`, `name`) VALUES (?, ?)"
                                + " AS new ON DUPLICATE KEY UPDATE `name` = new.`name`"),
                Arguments.of(
                        new SqliteDialect(),
                        "INSERT INTO \"users\" (\"email\", \"name\") VALUES (?, ?)"
                                + " ON CONFLICT (\"email\") DO UPDATE SET \"name\" = EXCLUDED.\"name\""));
    }

    @ParameterizedTest
    @MethodSource("singleColumnUpserts")
    void printsUpsert(Dialect dialect, String expected) {
        var query = insert(dialect).onConflict("email").doUpdate("name").toSql();

        assertEquals(expected, query.sql());
        assertEquals(List.of("rob@example.com", "rob"), query.params());
    }

    @Test
    void printsSeveralColumns() {
        var query =
                insert(new PostgresDialect())
                        .value("id", 1)
                        .value("age", 30)
                        .onConflict("id", "email")
                        .doUpdate("name", "age")
                        .toSql();

        assertEquals(
                "INSERT INTO \"users\" (\"email\", \"name\", \"id\", \"age\") VALUES ($1, $2, $3, $4)"
                        + " ON CONFLICT (\"id\", \"email\") DO UPDATE SET"
                        + " \"name\" = EXCLUDED.\"name\", \"age\" = EXCLUDED.\"age\"",
                query.sql());
        assertEquals(List.of("rob@example.com", "rob", 1, 30), query.params());

        assertEquals(
                "INSERT INTO `users` (`email`, `name`) VALUES (?, ?) AS new ON DUPLICATE KEY UPDATE"
                        + " `name` = new.`name`, `age` = new.`age`",
                insert(new MySqlDialect())
                        .onConflict("id", "email")
                        .doUpdate("name", "age")
                        .toSql()
                        .sql());
    }

    @Test
    void rejectsHalfOfUpsert() {
        var insert = insert(new PostgresDialect());

        assertThrows(QueryException.class, () -> insert.onConflict("email").toSql());
        assertThrows(QueryException.class, () -> insert.doUpdate("name").toSql());
    }

    @Test
    void rejectsUnknownColumns() {
        var insert = insert(new PostgresDialect());

        assertThrows(
                QueryException.class, () -> insert.onConflict("email").doUpdate("title").toSql());
        assertThrows(
                QueryException.class, () -> insert.onConflict("title").doUpdate("name").toSql());
        assertThrows(
                QueryException.class,
                () -> insert(new MySqlDialect()).onConflict("title").doUpdate("name").toSql());
    }

    @Test
    void standardDialectRejectsUpsert() {
        assertThrows(
                QueryException.class,
                () -> insert(new BaseDialect()).onConflict("email").doUpdate("name").toSql());
        assertEquals(
                "INSERT INTO \"users\" (\"email\", \"name\") VALUES (?, ?)",
                insert(new BaseDialect()).toSql().sql());
    }
}
