package hexlet.code;

import static hexlet.code.Conditions.eq;
import static hexlet.code.Conditions.lt;
import static hexlet.code.QueryBuilderFixtures.users;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModifyingQueriesTest {
    private final Table users = users();

    @Test
    void insertsRow() {
        var insert = users.insert().value("name", "rob").value("age", 30).toSql();

        assertEquals("INSERT INTO \"users\" (\"name\", \"age\") VALUES ($1, $2)", insert.sql());
        assertEquals(List.of("rob", 30), insert.params());
    }

    @Test
    void insertAcceptsNull() {
        var insert = users.insert().value("name", "rob").value("email", null).toSql();

        assertEquals(Arrays.asList("rob", null), insert.params());
    }

    @Test
    void updatesRows() {
        var update = users.update().set("name", "ann").set("age", 31).where(eq("id", 1)).toSql();

        assertEquals(
                "UPDATE \"users\" SET \"name\" = $1, \"age\" = $2 WHERE \"id\" = $3", update.sql());
        assertEquals(List.of("ann", 31, 1), update.params());
    }

    @Test
    void updateAcceptsNull() {
        var update = users.update().set("email", null).where(eq("id", 1)).toSql();

        assertEquals(Arrays.asList(null, 1), update.params());
    }

    @Test
    void deletesRowsWithRepeatedWhere() {
        var delete = users.delete().where(lt("age", 18)).where(eq("active", false)).toSql();

        assertEquals("DELETE FROM \"users\" WHERE \"age\" < $1 AND \"active\" = $2", delete.sql());
        assertEquals(List.of(18, false), delete.params());
    }

    @Test
    void wholeTableRequiresAll() {
        assertEquals("DELETE FROM \"users\"", users.delete().all().toSql().sql());

        var update = users.update().set("active", false).all().toSql();
        assertEquals("UPDATE \"users\" SET \"active\" = $1", update.sql());
        assertEquals(List.of(false), update.params());
    }

    @Test
    void missingConditionHasOwnException() {
        assertThrows(MissingConditionException.class, () -> users.delete().toSql());
        assertThrows(MissingConditionException.class, () -> users.update().set("age", 1).toSql());
        assertEquals(QueryException.class, MissingConditionException.class.getSuperclass());
    }

    @Test
    void rejectsEmptyInsertAndUpdate() {
        assertThrows(QueryException.class, () -> users.insert().toSql());
        assertThrows(QueryException.class, () -> users.update().where(eq("id", 1)).toSql());
        assertThrows(QueryException.class, () -> users.update().all().toSql());
    }

    @Test
    void rejectsUnknownColumns() {
        assertThrows(QueryException.class, () -> users.insert().value("title", "x").toSql());
        assertThrows(
                QueryException.class,
                () -> users.update().set("title", "x").where(eq("id", 1)).toSql());
        assertThrows(QueryException.class, () -> users.delete().where(eq("title", "x")).toSql());
    }

    @Test
    void modifyingQueriesAreImmutable() {
        var update = users.update().set("name", "ann");
        update.where(eq("id", 1));
        update.set("age", 3);
        assertThrows(MissingConditionException.class, update::toSql);
        assertEquals("UPDATE \"users\" SET \"name\" = $1", update.all().toSql().sql());

        var delete = users.delete();
        delete.where(eq("id", 1));
        delete.all();
        assertThrows(MissingConditionException.class, delete::toSql);

        var insert = users.insert().value("name", "rob");
        insert.value("age", 1);
        insert.onConflict("email");
        assertEquals("INSERT INTO \"users\" (\"name\") VALUES ($1)", insert.toSql().sql());
    }
}
