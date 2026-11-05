# Adventure Book

Interactive "choose your own adventure" application: browse a library of books, play one by choosing options
(health points, endings), save your progress and resume it later. Books are plain JSON files; a book that breaks the
rules (unknown section, dead end...) is still listed, flagged as invalid, and cannot be played.

## Repository layout

```
adventure-book/
├── adventure-book-frontend/            Angular 21 single-page app
└── adventure-book-backend-parent/      Maven parent
    ├── adventure-book-backend/         Spring Boot 3.5 REST API (+ MCP endpoint)
    ├── jpa-dynamic-filter-helper/      Dynamic search library (see below)
    └── jpa-dynamic-filter-sample/      Small demo of the library
```

## Prerequisites

- JDK 21 and Maven 3.9+
- Node.js 20.19+ (or 22.12+) and npm

## Build and run

The backend depends on `jpa-dynamic-filter-helper`, which must be installed in your local Maven repository first.

```bash
# 1. Library (once, and again whenever it changes)
cd adventure-book-backend-parent/jpa-dynamic-filter-helper
mvn install

# 2. Backend, on http://localhost:8080
cd ../adventure-book-backend
mvn spring-boot:run

# 3. Frontend, on http://localhost:4200 (proxies /api to the backend)
cd ../../adventure-book-frontend
npm install
npm start
```

Open <http://localhost:4200>. Run the backend tests with `mvn test` in `adventure-book-backend`.

The backend stores its data in an H2 file database (`adventure-book-backend/data/`, ignored by git) and loads the
books from `src/main/resources/books/*.json` at startup. New books can also be added from the UI ("Add a book (JSON)")
or with `POST /api/books`.

## Architecture

### Backend

Layered, with the domain at the center (`com.sbde.adventure`):

| Package          | Content                                                                              |
|------------------|--------------------------------------------------------------------------------------|
| `domain.book`    | `Book` aggregate (sections, options, validation), `BookService`, search criteria      |
| `domain.game`    | `Game` aggregate (health, current section, history), `GameService`, saved games       |
| `application`    | REST controllers, DTOs, and a minimal MCP server (`POST /mcp`) exposing the library  |
| `infrastructure` | Configuration and the startup `BookSeeder`                                           |

Main endpoints: `GET /api/books` (search, filters, paging), `GET /api/books/genres`, `POST /api/books`,
`POST /api/games`, `POST /api/games/{id}/choices`, `POST /api/games/{id}/save`, `POST /api/games/{id}/resume`,
`GET /api/games/saved`.

### Frontend

Standalone Angular components with signals: `features/home` (library, search, filters, saved games),
`features/game` (play screen), `shared` (book card) and `core` (models and HTTP services).

## Dynamic search: `jpa-dynamic-filter-helper`

The book search (text, difficulties, genres, validity, sorting, paging) is built with
[`jpa-dynamic-filter-helper`](adventure-book-backend-parent/jpa-dynamic-filter-helper), my own personal library, which I
plan to publish soon under an open-source license.

It turns a plain criteria object into a Spring Data `Specification`, so there is no hand-written query per filter
combination. Filters are declared with annotations, and empty values are ignored:

```java
@Sortby(sortBy = "title", direction = SortDirection.ASC)
public class BookSearchCriteria extends AbstractSearchCriteria {

    @FilterCriteria(fields = {"title", "author", "description"}, operator = Operator.LIKE, ignoreCase = true)
    private String search;

    @FilterCriteria(fields = "difficulty", operator = Operator.IN)
    private List<Difficulty> difficulties;

    @FilterCriteria(fields = "genres", operator = Operator.IN)
    private List<String> genres;
}
```

```java
Page<Book> page = books.findAll(new SearchSpecification<>(criteria.makeSearchRequest()), pageable);
```

It provides `EQUAL`, `NOT_EQUAL`, `LIKE`, `IN` (including element collections), `BETWEEN` ranges, case- and
accent-insensitive matching, multi-field search, sorting, optional fetch joins, and an `InternalPage` response wrapper.
