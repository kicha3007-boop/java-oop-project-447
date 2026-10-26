package hexlet.code;

import static hexlet.code.Conditions.and;
import static hexlet.code.Conditions.eq;
import static hexlet.code.Conditions.gt;
import static hexlet.code.QueryBuilderFixtures.SCHEMA;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DialectsTest {

    private static Table users(Dialect dialect) {
        return Sql.using(dialect, SCHEMA).table("users");
    }

    static Stream<Arguments> selects() {
        return Stream.of(
                Arguments.of(
                        new PostgresDialect(),
                        "SELECT \"name\" FROM \"users\" WHERE \"active\" = $1 AND \"age\" > $2"
                                + " ORDER BY \"name\" LIMIT 10"),
                Arguments.of(
                        new MySqlDialect(),
                        "SELECT `name` FROM `users` WHERE `active` = ? AND `age` > ?"
                                + " ORDER BY `name` LIMIT 10"),
                Arguments.of(
                        new BaseDialect(),
                        "SELECT \"name\" FROM \"users\" WHERE \"active\" = ? AND \"age\" > ?"
                                + " ORDER BY \"name\" LIMIT 10"),
                Arguments.of(
                        new SqliteDialect(),
                        "SELECT \"name\" FROM \"users\" WHERE \"active\" = ? AND \"age\" > ?"
                                + " ORDER BY \"name\" LIMIT 10"));
    }

    @ParameterizedTest
    @MethodSource("selects")
    void printsSelectInEveryDialect(Dialect dialect, String expected) {
        var query =
                users(dialect)
                        .select("name")
                        .where(and(eq("active", true), gt("age", 18)))
                        .orderBy("name")
                        .limit(10)
                        .toSql();

        assertEquals(expected, query.sql());
        assertEquals(List.of(true, 18), query.params());
    }

    static Stream<Arguments> updates() {
        return Stream.of(
                Arguments.of(
                        new PostgresDialect(),
                        "UPDATE \"users\" SET \"name\" = $1 WHERE \"id\" = $2"),
                Arguments.of(new MySqlDialect(), "UPDATE `users` SET `name` = ? WHERE `id` = ?"),
                Arguments.of(
                        new BaseDialect(), "UPDATE \"users\" SET \"name\" = ? WHERE \"id\" = ?"),
                Arguments.of(
                        new SqliteDialect(), "UPDATE \"users\" SET \"name\" = ? WHERE \"id\" = ?"));
    }

    @ParameterizedTest
    @MethodSource("updates")
    void printsUpdateInEveryDialect(Dialect dialect, String expected) {
        var query = users(dialect).update().set("name", "ann").where(eq("id", 1)).toSql();

        assertEquals(expected, query.sql());
        assertEquals(List.of("ann", 1), query.params());
    }

    static Stream<Arguments> insertsAndDeletes() {
        return Stream.of(
                Arguments.of(
                        new PostgresDialect(),
                        "INSERT INTO \"users\" (\"name\", \"age\") VALUES ($1, $2)",
                        "DELETE FROM \"users\" WHERE \"age\" < $1"),
                Arguments.of(
                        new MySqlDialect(),
                        "INSERT INTO `users` (`name`, `age`) VALUES (?, ?)",
                        "DELETE FROM `users` WHERE `age` < ?"),
                Arguments.of(
                        new BaseDialect(),
                        "INSERT INTO \"users\" (\"name\", \"age\") VALUES (?, ?)",
                        "DELETE FROM \"users\" WHERE \"age\" < ?"));
    }

    @ParameterizedTest
    @MethodSource("insertsAndDeletes")
    void printsInsertAndDeleteInEveryDialect(
            Dialect dialect, String expectedInsert, String expectedDelete) {
        var table = users(dialect);

        assertEquals(
                expectedInsert, table.insert().value("name", "rob").value("age", 30).toSql().sql());
        assertEquals(expectedDelete, table.delete().where(Conditions.lt("age", 18)).toSql().sql());
    }

    @Test
    void sameDialectGivesEqualQueries() {
        var first = users(new MySqlDialect()).select("id").toSql();
        var second = users(new MySqlDialect()).select("id").toSql();

        assertEquals(first, second);
    }

    @Test
    void quotesAreEscapedInsideIdentifiers() {
        assertEquals("\"a\"\"b\"", new PostgresDialect().quote("a\"b"));
        assertEquals("`a``b`", new MySqlDialect().quote("a`b"));
    }
}
