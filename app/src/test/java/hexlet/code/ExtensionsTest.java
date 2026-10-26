package hexlet.code;

import static hexlet.code.Conditions.and;
import static hexlet.code.Conditions.eq;
import static hexlet.code.Conditions.or;
import static hexlet.code.QueryBuilderFixtures.SCHEMA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hexlet.code.condition.Between;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExtensionsTest {
    private final Table postgres = Sql.using(new PostgresDialect(), SCHEMA).table("users");
    private final Table mysql = Sql.using(new MySqlDialect(), SCHEMA).table("users");
    private final Table sqlite = Sql.using(new SqliteDialect(), SCHEMA).table("users");

    @Test
    void sqliteUsesStandardQuotesAndPlaceholders() {
        var query = sqlite.select("name").where(eq("id", 1)).toSql();

        assertEquals("SELECT \"name\" FROM \"users\" WHERE \"id\" = ?", query.sql());
        assertEquals(List.of(1), query.params());
    }

    @Test
    void sqlitePrintsUpsertLikePostgres() {
        var query =
                sqlite.insert()
                        .value("email", "rob@example.com")
                        .value("name", "rob")
                        .onConflict("email")
                        .doUpdate("name")
                        .toSql();

        assertEquals(
                "INSERT INTO \"users\" (\"email\", \"name\") VALUES (?, ?)"
                        + " ON CONFLICT (\"email\") DO UPDATE SET \"name\" = EXCLUDED.\"name\"",
                query.sql());
    }

    @Test
    void betweenWorksInEveryDialect() {
        var condition = and(new Between("age", 18, 65), eq("active", true));

        var select = postgres.select("id").where(condition).toSql();
        assertEquals(
                "SELECT \"id\" FROM \"users\" WHERE \"age\" BETWEEN $1 AND $2 AND \"active\" = $3",
                select.sql());
        assertEquals(List.of(18, 65, true), select.params());

        assertEquals(
                "DELETE FROM `users` WHERE `age` BETWEEN ? AND ? AND `active` = ?",
                mysql.delete().where(condition).toSql().sql());
    }

    @Test
    void betweenNestsInOr() {
        var query =
                sqlite.select("id")
                        .where(and(eq("id", 1), or(new Between("age", 1, 2), eq("id", 3))))
                        .toSql();

        assertEquals(
                "SELECT \"id\" FROM \"users\" WHERE \"id\" = ? AND (\"age\" BETWEEN ? AND ?"
                        + " OR \"id\" = ?)",
                query.sql());
    }

    @Test
    void betweenRejectsNullBoundsAndUnknownColumn() {
        assertThrows(QueryException.class, () -> new Between("age", null, 65));
        assertThrows(QueryException.class, () -> new Between("age", 18, null));
        assertThrows(QueryException.class, () -> new Between(null, 18, 65));
        assertThrows(
                QueryException.class,
                () -> postgres.select("id").where(new Between("title", 1, 2)).toSql());
    }
}
