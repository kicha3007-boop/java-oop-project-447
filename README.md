# Конструктор SQL-запросов

[![hexlet-check](https://github.com/kicha3007-boop/java-oop-project-447/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/kicha3007-boop/java-oop-project-447/actions/workflows/hexlet-check.yml)
[![Java CI](https://github.com/kicha3007-boop/java-oop-project-447/actions/workflows/main.yml/badge.svg)](https://github.com/kicha3007-boop/java-oop-project-447/actions/workflows/main.yml)

Библиотека на Java, которая строит SQL-запрос из вызовов методов и печатает его для PostgreSQL,
MySQL и SQLite. Запрос не выполняется: библиотека возвращает текст с плейсхолдерами и отдельный список
значений, поэтому SQL-инъекция через значения невозможна. Имена таблиц и колонок проверяются по схеме
ещё до печати.

Учебный проект Хекслета: https://ru.hexlet.io/programs/java-oop

## Требования

- JDK 25
- Gradle ставить не нужно — в репозитории есть Gradle Wrapper (9.7.1)
- `make` (Linux/macOS или Git Bash/WSL на Windows)

## Сборка и проверка

```bash
git clone https://github.com/kicha3007-boop/java-oop-project-447.git
cd java-oop-project-447/app

make build      # сборка: тесты, линтер, порог покрытия 90%
make test       # только тесты JUnit
make lint       # линтер Spotless (google-java-format, стиль AOSP)
make lint-fix   # отформатировать код
make report     # отчёт о покрытии JaCoCo: build/reports/jacoco/test/html/index.html
```

Без `make` те же команды запускаются напрямую: `./gradlew clean build`, `./gradlew test`,
`./gradlew spotlessCheck`. Цели доступны и из корня репозитория (`make build`).

## Использование

```java
import static hexlet.code.Conditions.and;
import static hexlet.code.Conditions.eq;
import static hexlet.code.Conditions.gt;
import static hexlet.code.Conditions.or;

import hexlet.code.MySqlDialect;
import hexlet.code.PostgresDialect;
import hexlet.code.Schema;
import hexlet.code.Sql;
import java.util.List;
import java.util.Map;

var schema = Schema.of(Map.of("users", List.of("id", "name", "email", "age", "active")));
var users = Sql.using(new PostgresDialect(), schema).table("users");

var query = users.select("name")
        .where(and(eq("active", true), gt("age", 18)))
        .orderBy("name")
        .limit(10)
        .toSql();
query.sql();    // SELECT "name" FROM "users" WHERE "active" = $1 AND "age" > $2 ORDER BY "name" LIMIT 10
query.params(); // [true, 18]
```

### Условия

`eq`, `ne`, `gt`, `lt`, `in`, `isNull`, `and`, `or`, `not` из `hexlet.code.Conditions`. Скобки ставятся
только там, где без них меняется смысл:

```java
and(eq("active", true), or(eq("name", "ann"), eq("name", "rob")));
// "active" = $1 AND ("name" = $2 OR "name" = $3)

not(isNull("email"));
// NOT ("email" IS NULL)
```

Сравнение с `null` и пустой `in()` бросают `QueryException` — для пустых значений есть `isNull()`.

### Неизменяемые запросы

Каждый метод возвращает новый запрос, поэтому от базового можно строить варианты:

```java
var active = users.select("name").where(eq("active", true));
var adults = active.where(gt("age", 18));          // повторный where() соединяет через AND
var firstPage = active.orderBy("name").limit(10);  // active не изменился
```

### Вставка, обновление, удаление, upsert

```java
users.insert().value("name", "rob").value("age", 30).toSql().sql();
// INSERT INTO "users" ("name", "age") VALUES ($1, $2)

users.update().set("name", "ann").where(eq("id", 1)).toSql().sql();
// UPDATE "users" SET "name" = $1 WHERE "id" = $2

users.delete().all().toSql().sql();
// DELETE FROM "users"   (без where() и all() — MissingConditionException)

users.insert().value("email", "rob@example.com").value("name", "rob")
        .onConflict("email").doUpdate("name").toSql().sql();
// INSERT INTO "users" ("email", "name") VALUES ($1, $2) ON CONFLICT ("email") DO UPDATE SET "name" = EXCLUDED."name"
```

### Диалекты

| Диалект | Кавычки | Плейсхолдер | Upsert |
| --- | --- | --- | --- |
| `BaseDialect` (стандартный SQL) | `"name"` | `?` | не поддерживается, `QueryException` |
| `PostgresDialect` | `"name"` | `$1`, `$2` | `ON CONFLICT … DO UPDATE SET … = EXCLUDED.…` |
| `MySqlDialect` | `` `name` `` | `?` | `AS new ON DUPLICATE KEY UPDATE … = new.…` |
| `SqliteDialect` | `"name"` | `?` | как в PostgreSQL |

```java
Sql.using(new MySqlDialect(), schema).table("users")
        .update().set("name", "ann").where(eq("id", 1)).toSql().sql();
// UPDATE `users` SET `name` = ? WHERE `id` = ?
```

Общая раскладка запросов живёт в `BaseDialect` (шаблонный метод), подклассы переопределяют только
кавычки, плейсхолдеры и upsert.

### Своё условие

Условие — любой класс или лямбда, реализующие `Condition`. Печатает себя через публичные методы
`SqlWriter`: `column()` (проверка по схеме и кавычки), `param()` (значение в параметры) и `sql()`.
Пример в библиотеке — `hexlet.code.condition.Between`:

```java
var condition = and(new Between("age", 18, 65), eq("active", true));
users.select("id").where(condition).toSql().sql();
// SELECT "id" FROM "users" WHERE "age" BETWEEN $1 AND $2 AND "active" = $3
```

---

<details>
<summary>Автоматические тесты Хекслета</summary>

Тесты запускаются на каждый коммит. За запуск отвечает файл `.github/workflows/hexlet-check.yml` — не удаляйте и не переименовывайте ни его, ни репозиторий.

</details>

## О Хекслете

[Хекслет](https://ru.hexlet.io/) — школа программирования: авторские программы обучения с практикой, поддержкой наставников и реальными проектами, которые остаются в резюме. Этот репозиторий — один из таких проектов.
