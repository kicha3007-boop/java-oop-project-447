package hexlet.code;

import static hexlet.code.QueryBuilderFixtures.postgres;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class SelectTest {
    private final Sql db = postgres();

    @Test
    void selectsColumns() {
        var query = db.table("users").select("id", "name").toSql();

        assertEquals("SELECT \"id\", \"name\" FROM \"users\"", query.sql());
        assertEquals(List.of(), query.params());
    }

    @Test
    void selectsAllColumnsWithoutArguments() {
        assertEquals("SELECT * FROM \"posts\"", db.table("posts").select().toSql().sql());
    }

    @Test
    void equalQueriesCompileToEqualObjects() {
        var first = db.table("users").select("name").toSql();
        var second = db.table("users").select("name").toSql();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void rejectsUnknownTable() {
        assertThrows(QueryException.class, () -> db.table("orders").select("id").toSql());
        assertThrows(QueryException.class, () -> db.table("orders").select().toSql());
    }

    @Test
    void rejectsUnknownColumn() {
        assertThrows(QueryException.class, () -> db.table("posts").select("name").toSql());
    }

    @Test
    void rejectsNullColumn() {
        assertThrows(QueryException.class, () -> db.table("posts").select("id", null).toSql());
        assertThrows(QueryException.class, () -> db.table("posts").select((String[]) null));
    }

    @Test
    void queryExceptionIsRuntimeException() {
        assertEquals(RuntimeException.class, QueryException.class.getSuperclass());
    }

    @Test
    void compiledQueryParamsAreImmutable() {
        var query = db.table("users").select("id").toSql();

        assertThrows(UnsupportedOperationException.class, () -> query.params().add(1));
    }
}
