package hexlet.code;

import static hexlet.code.Conditions.and;
import static hexlet.code.Conditions.eq;
import static hexlet.code.Conditions.gt;
import static hexlet.code.Conditions.or;
import static hexlet.code.QueryBuilderFixtures.users;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class OrderLimitTest {
    private final Table users = users();

    @Test
    void printsPartsInFixedOrder() {
        var query =
                users.select("name")
                        .offset(20)
                        .limit(10)
                        .orderBy("name")
                        .where(eq("active", true))
                        .toSql();

        assertEquals(
                "SELECT \"name\" FROM \"users\" WHERE \"active\" = $1"
                        + " ORDER BY \"name\" LIMIT 10 OFFSET 20",
                query.sql());
        assertEquals(List.of(true), query.params());
    }

    @Test
    void fullExampleFromDescription() {
        var query =
                users.select("name")
                        .where(and(eq("active", true), gt("age", 18)))
                        .orderBy("name")
                        .limit(10)
                        .toSql();

        assertEquals(
                "SELECT \"name\" FROM \"users\" WHERE \"active\" = $1 AND \"age\" > $2"
                        + " ORDER BY \"name\" LIMIT 10",
                query.sql());
        assertEquals(List.of(true, 18), query.params());
    }

    @Test
    void orderByAppendsColumns() {
        assertEquals(
                "SELECT \"id\" FROM \"users\" ORDER BY \"name\", \"age\"",
                users.select("id").orderBy("name").orderBy("age").toSql().sql());
    }

    @Test
    void limitAndOffsetReplacePreviousValue() {
        assertEquals(
                "SELECT \"id\" FROM \"users\" LIMIT 5",
                users.select("id").limit(10).limit(5).toSql().sql());
        assertEquals(
                "SELECT \"id\" FROM \"users\" LIMIT 5 OFFSET 2",
                users.select("id").offset(9).limit(5).offset(2).toSql().sql());
    }

    @Test
    void limitZeroIsAllowed() {
        assertEquals(
                "SELECT \"id\" FROM \"users\" LIMIT 0 OFFSET 0",
                users.select("id").limit(0).offset(0).toSql().sql());
    }

    @Test
    void repeatedWhereJoinsWithAnd() {
        var query =
                users.select("id")
                        .where(or(eq("name", "ann"), eq("name", "rob")))
                        .where(gt("age", 18))
                        .toSql();

        assertEquals(
                "SELECT \"id\" FROM \"users\" WHERE (\"name\" = $1 OR \"name\" = $2)"
                        + " AND \"age\" > $3",
                query.sql());
        assertEquals(List.of("ann", "rob", 18), query.params());
    }

    @Test
    void branchesFromBaseQueryDoNotAffectEachOther() {
        var active = users.select("name").where(eq("active", true));
        var adults = active.where(gt("age", 18));
        var firstPage = active.orderBy("name").limit(10);

        assertEquals("SELECT \"name\" FROM \"users\" WHERE \"active\" = $1", active.toSql().sql());
        assertEquals(
                "SELECT \"name\" FROM \"users\" WHERE \"active\" = $1 AND \"age\" > $2",
                adults.toSql().sql());
        assertEquals(
                "SELECT \"name\" FROM \"users\" WHERE \"active\" = $1 ORDER BY \"name\" LIMIT 10",
                firstPage.toSql().sql());
    }

    @Test
    void everyMethodReturnsNewQuery() {
        var base = users.select("id");
        base.orderBy("name");
        base.limit(1);
        base.offset(1);
        base.where(eq("id", 1));

        assertEquals("SELECT \"id\" FROM \"users\"", base.toSql().sql());
    }

    @Test
    void rejectsInvalidLimitAndOffset() {
        assertThrows(QueryException.class, () -> users.select("id").limit(-1).toSql());
        assertThrows(QueryException.class, () -> users.select("id").limit(1).offset(-1).toSql());
        assertThrows(QueryException.class, () -> users.select("id").offset(20).toSql());
    }

    @Test
    void rejectsUnknownOrderByColumn() {
        assertThrows(QueryException.class, () -> users.select("id").orderBy("title").toSql());
        assertThrows(QueryException.class, () -> users.select("id").orderBy(null).toSql());
    }
}
