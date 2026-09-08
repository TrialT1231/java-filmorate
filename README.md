# java-filmorate

Бэкенд для сервиса Filmorate — приложения, которое хранит фильмы и оценки
пользователей, а также возвращает список фильмов, рекомендованных к
просмотру, на основе лайков.

## Стек технологий

- Java 21
- Spring Boot 3.5 (Web, Validation, JDBC)
- H2 Database (встроенная, файловое хранение в рабочем режиме)
- Lombok
- Zalando Logbook (логирование HTTP-запросов/ответов)
- JUnit 5, Mockito, Spring MockMvc — модульные тесты контроллеров
- Spring `@JdbcTest` — интеграционные тесты DAO-слоя

## Запуск приложения

```bash
mvn spring-boot:run
```

Приложение поднимается на `http://localhost:8080`. База данных хранится в
файле `./db/filmorate.mv.db` (создаётся автоматически при первом запуске) —
данные сохраняются между перезапусками. Схема (`schema.sql`) и справочные
данные (`data.sql`) применяются к базе при каждом старте приложения.

## Архитектура

Приложение построено по слоям:

- **model** — сущности приложения (`User`, `Film`, `Genre`, `Mpa`).
- **storage** — доступ к данным. Каждая сущность имеет интерфейс
  (`UserStorage`, `FilmStorage`, `GenreStorage`, `MpaStorage`) и его
  реализацию на JDBC (`UserDbStorage`, `FilmDbStorage`, `GenreDbStorage`,
  `MpaDbStorage`). Для пользователей и фильмов также есть in-memory
  реализации (`InMemoryUserStorage`, `InMemoryFilmStorage`), различаемые
  через `@Qualifier`.
- **service** — бизнес-логика (`UserService`, `FilmService`, `GenreService`,
  `MpaService`), зависит от интерфейсов хранилищ, а не от их реализаций.
- **controller** — REST-контроллеры, обрабатывают HTTP-запросы и делегируют
  логику сервисам.
- **exception** — `ValidationException`, `NotFoundException` и общий
  `ErrorHandler` для централизованной обработки ошибок.
- **validation** — кастомная аннотация `@ReleaseDateConstraint` для проверки
  минимальной даты релиза фильма.

## Схема базы данных

![ER-диаграмма](Untitled.png)

### Основные таблицы

- **users** — пользователи приложения.
- **films** — фильмы, каждый ссылается на свой возрастной рейтинг через
  `mpa_id`.
- **mpa** — справочник рейтингов MPA (`G`, `PG`, `PG-13`, `R`, `NC-17`).
- **genres** — справочник жанров (Комедия, Драма, Мультфильм, Триллер,
  Документальный, Боевик).
- **film_genres** — связь «многие ко многим» между фильмами и жанрами.
- **likes** — связь «многие ко многим» между пользователями и фильмами
  (лайки); правило «один пользователь — один лайк» обеспечивается составным
  первичным ключом `(film_id, user_id)`.
- **friendship** — **односторонняя** связь дружбы между пользователями.
  Если пользователь `user_id` добавил в друзья `friend_id`, это не означает,
  что `friend_id` тоже считает `user_id` своим другом — обратная запись не
  создаётся автоматически. Поле `status` зафиксировано в значении
  `CONFIRMED`, так как отдельного шага подтверждения заявки в приложении
  нет.

### Примеры запросов

**Получить все фильмы с названием рейтинга:**
```sql
SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.name AS mpa_name
FROM films f
JOIN mpa m ON f.mpa_id = m.mpa_id;
```

**Получить жанры конкретного фильма:**
```sql
SELECT g.name
FROM film_genres fg
JOIN genres g ON fg.genre_id = g.genre_id
WHERE fg.film_id = ?;
```

**Топ-N популярных фильмов по числу лайков:**
```sql
SELECT f.film_id, f.name, COUNT(l.user_id) AS likes_count
FROM films f
LEFT JOIN likes l ON f.film_id = l.film_id
GROUP BY f.film_id, f.name
ORDER BY likes_count DESC
LIMIT ?;
```

**Список друзей пользователя** (только те, кого он сам добавил):
```sql
SELECT u.*
FROM friendship fr
JOIN users u ON u.user_id = fr.friend_id
WHERE fr.user_id = ?;
```

**Общие друзья двух пользователей:**
```sql
SELECT u.*
FROM friendship fr1
JOIN friendship fr2 ON fr1.friend_id = fr2.friend_id
JOIN users u ON u.user_id = fr1.friend_id
WHERE fr1.user_id = ? AND fr2.user_id = ?;
```

**Добавление в друзья** (односторонняя запись, статус сразу `CONFIRMED`):
```sql
MERGE INTO friendship (user_id, friend_id, status)
KEY (user_id, friend_id)
VALUES (?, ?, 'CONFIRMED');
```

## REST API

### Пользователи

| Метод | Путь | Описание |
|---|---|---|
| GET | `/users` | Список всех пользователей |
| GET | `/users/{id}` | Пользователь по id |
| POST | `/users` | Создать пользователя |
| PUT | `/users` | Обновить пользователя |
| PUT | `/users/{id}/friends/{friendId}` | Добавить в друзья (односторонне) |
| DELETE | `/users/{id}/friends/{friendId}` | Удалить из друзей |
| GET | `/users/{id}/friends` | Список друзей пользователя |
| GET | `/users/{id}/friends/common/{otherId}` | Общие друзья с другим пользователем |

### Фильмы

| Метод | Путь | Описание |
|---|---|---|
| GET | `/films` | Список всех фильмов |
| GET | `/films/{id}` | Фильм по id |
| POST | `/films` | Добавить фильм |
| PUT | `/films` | Обновить фильм |
| PUT | `/films/{id}/like/{userId}` | Поставить лайк фильму |
| DELETE | `/films/{id}/like/{userId}` | Удалить лайк |
| GET | `/films/popular?count={count}` | Топ `count` фильмов по лайкам (по умолчанию 10) |

### Жанры и рейтинги

| Метод | Путь | Описание |
|---|---|---|
| GET | `/genres` | Список всех жанров |
| GET | `/genres/{id}` | Жанр по id |
| GET | `/mpa` | Список всех рейтингов MPA |
| GET | `/mpa/{id}` | Рейтинг MPA по id |

### Коды ответов

- `200 OK` — успешный запрос.
- `400 Bad Request` — ошибка валидации входных данных.
- `404 Not Found` — запрошенный объект не найден.
- `500 Internal Server Error` — непредвиденная ошибка сервера.

## Валидация

**Film:**
- название не может быть пустым;
- максимальная длина описания — 200 символов;
- дата релиза — не раньше 28 декабря 1895 года;
- продолжительность фильма должна быть положительным числом;
- у фильма должен быть указан рейтинг MPA.

**User:**
- электронная почта не может быть пустой и должна содержать символ `@`;
- логин не может быть пустым и содержать пробелы;
- имя для отображения может быть пустым — в таком случае используется логин;
- дата рождения не может быть в будущем.

## Тестирование

- **Модульные тесты контроллеров** (`FilmControllerTest`, `UserControllerTest`,
  `GenreControllerTest`, `MpaControllerTest`) — используют `@WebMvcTest` и
  `MockMvc`, сервисный слой замокан через `@MockBean`.
- **Интеграционные тесты DAO** (`UserDbStorageTest`, `FilmDbStorageTest`,
  `GenreDbStorageTest`, `MpaDbStorageTest`) — используют `@JdbcTest` с
  резидентной H2-базой; перед каждым тестом Spring поднимает чистую БД и
  применяет `schema.sql`/`data.sql`.

Запуск всех тестов:
```bash
mvn test
```