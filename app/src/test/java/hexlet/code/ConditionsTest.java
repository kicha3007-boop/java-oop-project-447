package hexlet.code;

import static hexlet.code.Conditions.and;
import static hexlet.code.Conditions.eq;
import static hexlet.code.Conditions.gt;
import static hexlet.code.Conditions.in;
import static hexlet.code.Conditions.isNull;
import static hexlet.code.Conditions.lt;
import static hexlet.code.Conditions.ne;
import static hexlet.code.Conditions.not;
import static hexlet.code.Conditions.or;
import static hexlet.code.QueryBuilderFixtures.users;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConditionsTest {
    private static final String PREFIX = "SELECT \"name\" FROM \"users\" WHERE ";

    private static CompiledQuery where(Condition condition) {
        return users().select("name").where(condition).toSql();
    }

    @Test
    void valuesGoToParams() {
        var query = where(eq("id", 1));

        assertEquals(PREFIX + "\"id\" = $1", query.sql());
        assertEquals(List.of(1), query.params());
    }

    static Stream<Arguments> operators() {
        return Stream.of(
                Arguments.of(eq("id", 1), "\"id\" = $1", List.of(1)),
                Arguments.of(ne("name", "ann"), "\"name\" <> $1", List.of("ann")),
                Arguments.of(gt("age", 18), "\"age\" > $1", List.of(18)),
                Arguments.of(lt("age", 65), "\"age\" < $1", List.of(65)),
                Arguments.of(in("id", 1, 2, 3), "\"id\" IN ($1, $2, $3)", List.of(1, 2, 3)),
                Arguments.of(isNull("email"), "\"email\" IS NULL", List.of()),
                Arguments.of(
                        and(eq("active", true), gt("age", 18), in("id", 5, 7)),
                        "\"active\" = $1 AND \"age\" > $2 AND \"id\" IN ($3, $4)",
                        List.of(true, 18, 5, 7)),
                Arguments.of(not(isNull("email")), "NOT (\"email\" IS NULL)", List.of()),
                Arguments.of(
                        and(eq("active", true), or(eq("name", "ann"), eq("name", "rob"))),
                        "\"active\" = $1 AND (\"name\" = $2 OR \"name\" = $3)",
                        List.of(true, "ann", "rob")),
                Arguments.of(
                        or(and(eq("active", true), gt("age", 18)), eq("id", 1)),
                        "\"active\" = $1 AND \"age\" > $2 OR \"id\" = $3",
                        List.of(true, 18, 1)),
                Arguments.of(
                        not(or(eq("id", 1), eq("id", 2))),
                        "NOT (\"id\" = $1 OR \"id\" = $2)",
                        List.of(1, 2)),
                Arguments.of(
                        and(and(eq("id", 1), eq("age", 2)), eq("name", "ann")),
                        "\"id\" = $1 AND \"age\" = $2 AND \"name\" = $3",
                        List.of(1, 2, "ann")),
                Arguments.of(
                        and(eq("id", 1), or(or(eq("id", 2), eq("id", 3)))),
                        "\"id\" = $1 AND (\"id\" = $2 OR \"id\" = $3)",
                        List.of(1, 2, 3)),
                Arguments.of(
                        and(eq("id", 1), and(or(eq("id", 2), eq("id", 3)))),
                        "\"id\" = $1 AND (\"id\" = $2 OR \"id\" = $3)",
                        List.of(1, 2, 3)));
    }

    @ParameterizedTest
    @MethodSource("operators")
    void rendersCondition(Condition condition, String expectedSql, List<Object> expectedParams) {
        var query = where(condition);

        assertEquals(PREFIX + expectedSql, query.sql());
        assertEquals(expectedParams, query.params());
    }

    @Test
    void conditionIsReusable() {
        var adults = gt("age", 18);

        assertEquals(PREFIX + "\"age\" > $1", where(adults).sql());
        assertEquals(
                PREFIX + "\"id\" = $1 AND \"age\" > $2", where(and(eq("id", 1), adults)).sql());
    }

    @Test
    void lambdaIsCondition() {
        Condition startsWithA = writer -> writer.column("name").sql(" LIKE ").param("a%");

        var query = where(and(startsWithA, eq("active", true)));

        assertEquals(PREFIX + "\"name\" LIKE $1 AND \"active\" = $2", query.sql());
        assertEquals(List.of("a%", true), query.params());
    }

    static Stream<Executable> invalidConditions() {
        return Stream.of(
                () -> eq("email", null),
                () -> ne("email", null),
                () -> gt("age", null),
                () -> lt("age", null),
                () -> in("id", 1, null),
                () -> in("id", (Object[]) null),
                () -> in("id"),
                () -> eq(null, 1),
                () -> isNull(null),
                () -> and(),
                () -> or(),
                () -> and(eq("id", 1), null),
                () -> not(null));
    }

    @ParameterizedTest
    @MethodSource("invalidConditions")
    void rejectsInvalidCondition(Executable creation) {
        assertThrows(QueryException.class, creation);
    }

    @Test
    void rejectsUnknownColumnInCondition() {
        assertThrows(QueryException.class, () -> where(eq("title", "x")));
        assertThrows(QueryException.class, () -> where(not(isNull("title"))));
    }

    @Test
    void rejectsNullWhere() {
        assertThrows(QueryException.class, () -> users().select("id").where(null).toSql());
    }
}
