/* Qurium - 2026 */
package org.qurium.schema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.qurium.common.exception.QuriumException;

@QuarkusTest
class DDLParserServiceTest {

    @Inject DDLParserService parser;
    @Inject ObjectMapper objectMapper;

    @Test
    void parse_createTable_extractsTableName() throws Exception {
        String ddl = "CREATE TABLE users (id SERIAL PRIMARY KEY, name VARCHAR(255));";

        List<Map<String, Object>> result = parse(ddl);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("table")).isEqualTo("users");
    }

    @Test
    void parse_createTable_extractsColumns() throws Exception {
        String ddl =
                "CREATE TABLE users (id SERIAL PRIMARY KEY, name VARCHAR(255), email VARCHAR(255)"
                        + " NOT NULL);";

        List<Map<String, Object>> result = parse(ddl);

        List<Map<String, String>> columns = columns(result, "users");
        assertThat(columns).hasSize(3);
        assertThat(columns.get(0)).containsEntry("name", "id");
        assertThat(columns.get(1)).containsEntry("name", "name");
        assertThat(columns.get(2)).containsEntry("name", "email");
    }

    @Test
    void parse_createTable_setsNullableCorrectly() throws Exception {
        String ddl =
                "CREATE TABLE users (id SERIAL PRIMARY KEY, name VARCHAR(255), email VARCHAR(255)"
                        + " NOT NULL);";

        List<Map<String, Object>> result = parse(ddl);

        List<Map<String, String>> columns = columns(result, "users");
        assertThat(columns.get(1)).containsEntry("nullable", "YES");
        assertThat(columns.get(2)).containsEntry("nullable", "NO");
    }

    @Test
    void parse_createTable_multipleTablesExtracted() throws Exception {
        String ddl =
                """
                CREATE TABLE users (id SERIAL PRIMARY KEY);
                CREATE TABLE orders (id SERIAL PRIMARY KEY);
                """;

        List<Map<String, Object>> result = parse(ddl);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(t -> t.get("table"))
                .containsExactlyInAnyOrder("users", "orders");
    }

    @Test
    void parse_createTable_ifNotExistsSyntax() throws Exception {
        String ddl = "CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY);";

        List<Map<String, Object>> result = parse(ddl);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("table")).isEqualTo("users");
    }

    @Test
    void parse_createTable_skipsInlineConstraints() throws Exception {
        String ddl =
                """
                CREATE TABLE orders (
                    id SERIAL,
                    user_id INT,
                    PRIMARY KEY (id),
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    CONSTRAINT uq_order UNIQUE (id)
                );
                """;

        List<Map<String, Object>> result = parse(ddl);

        List<Map<String, String>> columns = columns(result, "orders");
        assertThat(columns).hasSize(2);
        assertThat(columns).extracting(c -> c.get("name")).containsExactly("id", "user_id");
    }

    @Test
    void parse_alterAddColumn_appendsColumnToExistingTable() throws Exception {
        String ddl =
                """
                CREATE TABLE users (id SERIAL PRIMARY KEY);
                ALTER TABLE users ADD COLUMN email VARCHAR(255);
                """;

        List<Map<String, Object>> result = parse(ddl);

        List<Map<String, String>> columns = columns(result, "users");
        assertThat(columns).hasSize(2);
        assertThat(columns.get(1))
                .containsEntry("name", "email")
                .containsEntry("type", "VARCHAR(255)");
    }

    @Test
    void parse_alterAddColumn_notNullSetCorrectly() throws Exception {
        String ddl =
                """
                CREATE TABLE users (id SERIAL PRIMARY KEY);
                ALTER TABLE users ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
                """;

        List<Map<String, Object>> result = parse(ddl);

        List<Map<String, String>> columns = columns(result, "users");
        assertThat(columns.get(1)).containsEntry("nullable", "NO");
    }

    @Test
    void parse_alterAddColumn_unknownTableIgnored() throws Exception {
        String ddl =
                """
                CREATE TABLE users (id SERIAL PRIMARY KEY);
                ALTER TABLE nonexistent ADD COLUMN foo VARCHAR(10);
                """;

        List<Map<String, Object>> result = parse(ddl);

        assertThat(columns(result, "users")).hasSize(1);
    }

    @Test
    void parse_alterAddForeignKey_addsConstraint() throws Exception {
        String ddl =
                """
                CREATE TABLE users (id SERIAL PRIMARY KEY);
                CREATE TABLE orders (id SERIAL PRIMARY KEY, user_id INT);
                ALTER TABLE orders ADD CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id);
                """;

        List<Map<String, Object>> result = parse(ddl);

        List<Map<String, Object>> constraints = constraints(result, "orders");
        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0))
                .containsEntry("name", "fk_orders_user")
                .containsEntry("type", "FOREIGN_KEY")
                .containsEntry("columns", "user_id")
                .containsEntry("referencesTable", "users")
                .containsEntry("referencesColumns", "id");
    }

    @Test
    void parse_alterAddUniqueConstraint_addsConstraint() throws Exception {
        String ddl =
                """
                CREATE TABLE users (id SERIAL PRIMARY KEY, email VARCHAR(255));
                ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);
                """;

        List<Map<String, Object>> result = parse(ddl);

        List<Map<String, Object>> constraints = constraints(result, "users");
        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0))
                .containsEntry("name", "uq_users_email")
                .containsEntry("type", "UNIQUE")
                .containsEntry("columns", "email");
    }

    @Test
    void parse_createIndex_addsIndexToTable() throws Exception {
        String ddl =
                """
                CREATE TABLE orders (id SERIAL PRIMARY KEY, status VARCHAR(50));
                CREATE INDEX idx_orders_status ON orders (status);
                """;

        List<Map<String, Object>> result = parse(ddl);

        List<Map<String, Object>> indexes = indexes(result, "orders");
        assertThat(indexes).hasSize(1);
        assertThat(indexes.get(0))
                .containsEntry("name", "idx_orders_status")
                .containsEntry("columns", "status")
                .containsEntry("unique", false);
    }

    @Test
    void parse_createUniqueIndex_markedAsUnique() throws Exception {
        String ddl =
                """
                CREATE TABLE users (id SERIAL PRIMARY KEY, email VARCHAR(255));
                CREATE UNIQUE INDEX idx_users_email ON users (email);
                """;

        List<Map<String, Object>> result = parse(ddl);

        List<Map<String, Object>> indexes = indexes(result, "users");
        assertThat(indexes.get(0)).containsEntry("unique", true);
    }

    @Test
    void parse_insertInto_extractsSampleData() throws Exception {
        String ddl =
                """
                CREATE TABLE users (id SERIAL PRIMARY KEY, name VARCHAR(255));
                INSERT INTO users (id, name) VALUES (1, 'Alice'), (2, 'Bob');
                """;

        List<Map<String, Object>> result = parse(ddl);

        List<Map<String, String>> sampleData = sampleData(result, "users");
        assertThat(sampleData).hasSize(2);
        assertThat(sampleData.get(0)).containsEntry("id", "1").containsEntry("name", "Alice");
        assertThat(sampleData.get(1)).containsEntry("id", "2").containsEntry("name", "Bob");
    }

    @Test
    void parse_insertInto_unknownTableIgnored() throws Exception {
        String ddl =
                """
                CREATE TABLE users (id SERIAL PRIMARY KEY);
                INSERT INTO nonexistent (id) VALUES (1);
                """;

        List<Map<String, Object>> result = parse(ddl);

        assertThat(result.get(0).get("sampleData")).isNull();
    }

    @Test
    void parse_inlineIndex_addsIndexAndNotColumn() throws Exception {
        String ddl =
                """
                CREATE TABLE users (
                    id INT NOT NULL,
                    email VARCHAR(255),
                    INDEX idx_email (email)
                );
                """;

        List<Map<String, Object>> result = parse(ddl);

        List<Map<String, String>> columns = columns(result, "users");
        assertThat(columns).hasSize(2);
        assertThat(columns).extracting(c -> c.get("name")).doesNotContain("INDEX");

        List<Map<String, Object>> indexes = indexes(result, "users");
        assertThat(indexes).hasSize(1);
        assertThat(indexes.get(0))
                .containsEntry("name", "idx_email")
                .containsEntry("columns", "email")
                .containsEntry("unique", false);
    }

    @Test
    void parse_inlineUniqueKey_addsUniqueIndexAndNotColumn() throws Exception {
        String ddl =
                """
                CREATE TABLE users (
                    id INT NOT NULL,
                    username VARCHAR(255),
                    UNIQUE KEY uq_username (username)
                );
                """;

        List<Map<String, Object>> result = parse(ddl);

        List<Map<String, String>> columns = columns(result, "users");
        assertThat(columns).hasSize(2);

        List<Map<String, Object>> indexes = indexes(result, "users");
        assertThat(indexes).hasSize(1);
        assertThat(indexes.get(0))
                .containsEntry("name", "uq_username")
                .containsEntry("columns", "username")
                .containsEntry("unique", true);
    }

    @Test
    void parse_inlineForeignKey_addsConstraintAndNotColumn() throws Exception {
        String ddl =
                """
                CREATE TABLE orders (
                    id INT NOT NULL,
                    user_id INT NOT NULL,
                    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
                );
                """;

        List<Map<String, Object>> result = parse(ddl);

        List<Map<String, String>> columns = columns(result, "orders");
        assertThat(columns).hasSize(2);

        List<Map<String, Object>> constraints = constraints(result, "orders");
        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0))
                .containsEntry("name", "fk_orders_user")
                .containsEntry("type", "FOREIGN_KEY")
                .containsEntry("columns", "user_id")
                .containsEntry("referencesTable", "users")
                .containsEntry("referencesColumns", "id");
    }

    @Test
    void parse_mysqlStyle_fullSchema() throws Exception {
        String ddl =
                """
                CREATE TABLE user_details (
                    user_id INT(11) NOT NULL AUTO_INCREMENT,
                    username VARCHAR(255) DEFAULT NULL,
                    PRIMARY KEY (user_id),
                    UNIQUE KEY uq_username (username)
                );
                CREATE TABLE roles (
                    role_id INT NOT NULL AUTO_INCREMENT,
                    role_name VARCHAR(50) NOT NULL,
                    PRIMARY KEY (role_id)
                );
                CREATE TABLE user_addresses (
                    address_id INT NOT NULL AUTO_INCREMENT,
                    user_id INT(11) NOT NULL,
                    role_id INT NOT NULL,
                    city VARCHAR(100) NOT NULL,
                    PRIMARY KEY (address_id),
                    INDEX idx_user_id (user_id),
                    INDEX idx_role_id (role_id),
                    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES user_details(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
                    CONSTRAINT fk_address_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT ON UPDATE CASCADE
                );
                """;

        List<Map<String, Object>> result = parse(ddl);

        assertThat(result).hasSize(3);

        // user_details: UNIQUE KEY becomes an index, not a column
        assertThat(columns(result, "user_details")).hasSize(2);
        assertThat(indexes(result, "user_details")).hasSize(1);
        assertThat(indexes(result, "user_details").get(0)).containsEntry("unique", true);

        // user_addresses: INDEX entries become indexes, not columns; CONSTRAINT entries become constraints
        List<Map<String, String>> addrColumns = columns(result, "user_addresses");
        assertThat(addrColumns).hasSize(4);
        assertThat(addrColumns).extracting(c -> c.get("name")).doesNotContain("INDEX", "CONSTRAINT");

        assertThat(indexes(result, "user_addresses")).hasSize(2);
        assertThat(constraints(result, "user_addresses")).hasSize(2);
        assertThat(constraints(result, "user_addresses").get(0))
                .containsEntry("referencesTable", "user_details");
        assertThat(constraints(result, "user_addresses").get(1))
                .containsEntry("referencesTable", "roles");
    }

    @Test
    void parse_noCreateTable_throwsException() {
        String ddl = "INSERT INTO users (id) VALUES (1);";

        assertThatThrownBy(() -> parser.parse(ddl)).isInstanceOf(QuriumException.class);
    }

    @Test
    void parse_emptyString_throwsException() {
        assertThatThrownBy(() -> parser.parse("")).isInstanceOf(QuriumException.class);
    }

    private List<Map<String, Object>> parse(String ddl) throws Exception {
        String json = parser.parse(ddl);
        return objectMapper.readValue(json, new TypeReference<>() {});
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> columns(List<Map<String, Object>> tables, String tableName) {
        return tables.stream()
                .filter(t -> tableName.equals(t.get("table")))
                .findFirst()
                .map(t -> (List<Map<String, String>>) t.get("columns"))
                .orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> constraints(
            List<Map<String, Object>> tables, String tableName) {
        return tables.stream()
                .filter(t -> tableName.equals(t.get("table")))
                .findFirst()
                .map(t -> (List<Map<String, Object>>) t.get("constraints"))
                .orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> indexes(List<Map<String, Object>> tables, String tableName) {
        return tables.stream()
                .filter(t -> tableName.equals(t.get("table")))
                .findFirst()
                .map(t -> (List<Map<String, Object>>) t.get("indexes"))
                .orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> sampleData(
            List<Map<String, Object>> tables, String tableName) {
        return tables.stream()
                .filter(t -> tableName.equals(t.get("table")))
                .findFirst()
                .map(t -> (List<Map<String, String>>) t.get("sampleData"))
                .orElseThrow();
    }
}
