# Qurium API — Implementation Plan

## Context

Qurium is an API that lets users connect to external databases, import their schemas, and ask natural-language questions. The AI translates each question into SQL, executes it against the user's database, and returns the generated SQL, query results, and an explanation. The tech stack is Quarkus 3.34.5 / Java 25 with PostgreSQL for Qurium's own metadata. Architecture is **Command Query Separation (CQS)**: every operation is either a Command POJO + handler or a Query POJO + handler. The AI module uses `quarkus-langchain4j-anthropic`.

---

## Package Structure

```
org.qurium/
├── common/
│   ├── EncryptionService.java        # AES-GCM encrypt/decrypt for DB passwords
│   └── PagedResponse.java            # generic Nrecord(items, total, page, size)
├── connections/
│   ├── domain/DatabaseConnection.java  # PanacheEntity
│   ├── command/  CreateConnectionCommand, DeleteConnectionCommand + handlers
│   ├── query/    GetConnectionQuery, ListConnectionsQuery, TestConnectionQuery + handlers
│   ├── dto/      ConnectionRequest, ConnectionResponse, TestConnectionResponse
│   └── ConnectionResource.java
├── schema/
│   ├── domain/Schema.java              # PanacheEntity — stores schemaJson as TEXT
│   ├── command/  ImportSchemaFromConnectionCommand, UploadSchemaDDLCommand + handlers
│   ├── query/    GetSchemaQuery + handler
│   ├── dto/      UploadSchemaDDLRequest, SchemaResponse
│   ├── SchemaIntrospectionService.java # reads DatabaseMetaData via JDBC
│   └── SchemaResource.java
├── nlquery/
│   ├── domain/NlQuery.java             # PanacheEntity — stores question, sql, result, explanation
│   ├── command/  ExecuteNlQueryCommand + handler  (core orchestration)
│   ├── query/    GetQueryHistoryQuery, GetQueryByIdQuery + handlers
│   ├── dto/      NlQueryRequest, NlQueryResponse, QueryHistoryResponse
│   └── NlQueryResource.java
└── ai/
    ├── AiSqlService.java               # @RegisterAiService interface (LangChain4j)
    ├── AiSqlResponse.java              # record(sql, explanation)
    └── DatabaseConnectionFactory.java  # builds java.sql.Connection at runtime via DriverManager
```

---

## API Endpoints

| Method | Path | Operation |
|--------|------|-----------|
| POST | `/api/connections` | CreateConnectionCommand |
| GET | `/api/connections` | ListConnectionsQuery |
| GET | `/api/connections/{id}` | GetConnectionQuery |
| DELETE | `/api/connections/{id}` | DeleteConnectionCommand |
| POST | `/api/connections/{id}/test` | TestConnectionQuery |
| POST | `/api/connections/{id}/schema/introspect` | ImportSchemaFromConnectionCommand |
| POST | `/api/connections/{id}/schema` | UploadSchemaDDLCommand |
| GET | `/api/connections/{id}/schema` | GetSchemaQuery |
| POST | `/api/connections/{id}/query` | ExecuteNlQueryCommand |
| GET | `/api/connections/{id}/query/history` | GetQueryHistoryQuery |
| GET | `/api/connections/{id}/query/{queryId}` | GetQueryByIdQuery |

---

## Domain Entities

### `DatabaseConnection` (`database_connections` table)
Fields: `name`, `type` (enum: POSTGRESQL/MYSQL), `host`, `port`, `dbName`, `username`, `encryptedPassword`, `createdAt`, `updatedAt`

### `Schema` (`schemas` table)
Fields: `connection` (FK), `schemaJson` (TEXT — JSON array of table/column metadata), `source` (enum: INTROSPECTED/UPLOADED_DDL), `createdAt`

### `NlQuery` (`nl_queries` table)
Fields: `connection` (FK), `question`, `generatedSql`, `resultSnapshot` (TEXT — JSON rows, capped at 1000), `explanation`, `status` (enum: PENDING/SUCCESS/FAILED), `errorMessage`, `executedAt`

---

## Key Implementation Details

### pom.xml additions
```xml
<!-- In <dependencyManagement> after quarkus-bom -->
<dependency>
  <groupId>io.quarkiverse.langchain4j</groupId>
  <artifactId>quarkus-langchain4j-bom</artifactId>
  <version>0.24.0</version>
  <type>pom</type><scope>import</scope>
</dependency>

<!-- In <dependencies> -->
<dependency>
  <groupId>io.quarkiverse.langchain4j</groupId>
  <artifactId>quarkus-langchain4j-anthropic</artifactId>
</dependency>
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
  <version>9.2.0</version>
</dependency>
```

### application.properties additions
```properties
quarkus.hibernate-orm.database.generation=update
quarkus.langchain4j.anthropic.api-key=${ANTHROPIC_API_KEY}
quarkus.langchain4j.anthropic.chat-model.model-name=claude-sonnet-4-6
quarkus.langchain4j.anthropic.chat-model.max-tokens=4096
quirkum.encryption.secret=${QURIUM_ENCRYPTION_SECRET}
```

### AI Service interface
```java
@RegisterAiService
public interface AiSqlService {
    @SystemMessage("You are an expert SQL assistant. Given a schema and question, respond ONLY with JSON: {\"sql\": \"...\", \"explanation\": \"...\"}")
    @UserMessage("Schema:\n{{schema}}\n\nQuestion: {{question}}")
    AiSqlResponse generateSql(@V("schema") String schema, @V("question") String question);
}
```

### ExecuteNlQueryHandler orchestration
1. Load `DatabaseConnection` — 404 if missing
2. Load `Schema` — 400 if no schema imported yet
3. Persist `NlQuery` with `status=PENDING`
4. Call `AiSqlService.generateSql(schema.schemaJson, question)`
5. Validate AI returned a SELECT/WITH statement (reject anything else)
6. Open a dynamic JDBC connection via `DatabaseConnectionFactory`, execute SQL, serialize rows as JSON (cap at 1000 rows)
7. Update `NlQuery` to `SUCCESS` with results and explanation — or `FAILED` with error message
8. Return `NlQueryResponse`

### EncryptionService
- AES-256-GCM, 96-bit random IV prepended to ciphertext, Base64-encoded
- Key from `QURIUM_ENCRYPTION_SECRET` env var (32-byte base64)

### DatabaseConnectionFactory
- Builds JDBC URL from `DatabaseConnection.type/host/port/dbName`
- Decrypts password via `EncryptionService`
- Uses `DriverManager.getConnection` (no pooling — per-query connections only)
- `verify(type, host, port, dbName, username, password)` — opens and immediately closes a connection to validate credentials; throws `DATABASE_CONNECTION_UNREACHABLE` (400) on failure
- `open(DatabaseConnection)` — opens and returns a live `Connection` for query execution (caller is responsible for closing it)

---

## Implementation Sequence

**Phase 1 — Foundation**
1. Add pom.xml BOM + dependencies; verify `./mvnw package -DskipTests`
2. Update `application.properties`
3. `EncryptionService` + unit test
4. `PagedResponse` record
5. `DatabaseConnection` entity + `DatabaseType` enum → verify table creation
6. `DatabaseConnectionFactory` — `verify()` + `open()`
7. Connections DTOs, commands, queries, handlers, `ConnectionResource`
   - `POST /api/connections` verifies the live connection before persisting; returns 400 (`DATABASE_CONNECTION_UNREACHABLE`) if unreachable
   - `POST /api/connections/{id}/test` re-tests an already-saved connection (e.g. after a password rotation or server restart)
8. Smoke-test all connection endpoints

**Phase 2 — Schema**
8. `Schema` entity + `SchemaSource` enum
9. `SchemaIntrospectionService` (JDBC `DatabaseMetaData`)
10. Schema commands, queries, handlers, DTOs, `SchemaResource`
11. Test introspect and DDL-upload endpoints

**Phase 3 — AI + NlQuery**
12. `AiSqlResponse` + `AiSqlService`
13. `NlQuery` entity + `QueryStatus` enum
14. NlQuery DTOs, `ExecuteNlQueryHandler`, history/getById handlers, `NlQueryResource`
15. End-to-end test: create connection → introspect → ask question → verify response

**Phase 4 — Hardening**
16. `GlobalExceptionMapper` (`@Provider ExceptionMapper<Exception>`)
17. Bean validation (`@NotBlank`, `@NotNull`) on all request DTOs
18. SELECT-only guard in query execution
19. Add `ANTHROPIC_API_KEY` + `QURIUM_ENCRYPTION_SECRET` to `.env` and `docker-compose.yml`
20. Delete `ExampleResource.java` and `MyEntity.java`

---

## Verification

```bash
# Start database
docker compose up db

# Run in dev mode
./mvnw quarkus:dev

# Create a connection
curl -X POST http://localhost:8080/api/connections \
  -H "Content-Type: application/json" \
  -d '{"name":"test","type":"POSTGRESQL","host":"localhost","port":5432,"dbName":"mydb","username":"user","password":"pass"}'

# Test the connection
curl -X POST http://localhost:8080/api/connections/1/test

# Introspect schema
curl -X POST http://localhost:8080/api/connections/1/schema/introspect

# Ask a natural language question
curl -X POST http://localhost:8080/api/connections/1/query \
  -H "Content-Type: application/json" \
  -d '{"question":"How many users signed up last month?"}'
# Expected: { "generatedSql": "...", "resultSnapshot": [...], "explanation": "..." }

# Run tests
./mvnw test
```