/* Qurium - 2026 */
package org.qurium.connection.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.domain.DatabaseConnectionType;
import org.qurium.connection.dto.requests.CreateDatabaseConnectionRequest;

@QuarkusTest
class DatabaseConnectionRepositoryTest {

    @Inject DatabaseConnectionRepository repository;

    @BeforeEach
    @Transactional
    void setUp() {
        repository.getEntityManager()
                .createNativeQuery("DELETE FROM database_connection")
                .executeUpdate();
    }

    @Test
    @Transactional
    void store_validRequest_returnsUuid() {
        UUID id = repository.store(aRequest("My Connection", null, null));

        assertThat(id).isNotNull();
    }

    @Test
    @Transactional
    void store_persistsAllFields() {
        UUID id = repository.store(aRequest("Prod DB", "admin", "secret"));

        DatabaseConnection saved = repository.findByIdOptional(id).orElseThrow();

        assertThat(saved.getName()).isEqualTo("Prod DB");
        assertThat(saved.getType()).isEqualTo(DatabaseConnectionType.POSTGRES);
        assertThat(saved.getHost()).isEqualTo("localhost");
        assertThat(saved.getPort()).isEqualTo(5432L);
        assertThat(saved.getDatabaseName()).isEqualTo("testdb");
        assertThat(saved.getUsername()).isEqualTo("admin");
        assertThat(saved.getEncryptedPassword()).isEqualTo("secret");
    }

    @Test
    @Transactional
    void store_withoutCredentials_persistsNullUsernameAndPassword() {
        UUID id = repository.store(aRequest("No Creds", null, null));

        DatabaseConnection saved = repository.findByIdOptional(id).orElseThrow();

        assertThat(saved.getUsername()).isNull();
        assertThat(saved.getEncryptedPassword()).isNull();
    }

    @Test
    @Transactional
    void findByIdOptional_existingId_returnsConnection() {
        UUID id = repository.store(aRequest("My Connection", null, null));

        Optional<DatabaseConnection> result = repository.findByIdOptional(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    void findByIdOptional_nonExistentId_returnsEmpty() {
        Optional<DatabaseConnection> result = repository.findByIdOptional(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    void findByIdOptional_softDeletedId_returnsEmpty() {
        UUID id = repository.store(aRequest("To Delete", null, null));
        DatabaseConnection saved = repository.findByIdOptional(id).orElseThrow();
        saved.delete();
        repository.getEntityManager().flush();
        repository.getEntityManager().clear();

        Optional<DatabaseConnection> result = repository.findByIdOptional(id);

        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    void findAll_returnsOnlyActiveConnections() {
        repository.store(aRequest("Active A", null, null));
        repository.store(aRequest("Active B", null, null));

        List<DatabaseConnection> results = repository.findAll().list();

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(DatabaseConnection::getName)
                .containsExactlyInAnyOrder("Active A", "Active B");
    }

    @Test
    @Transactional
    void findAll_excludesSoftDeletedConnections() {
        UUID activeId = repository.store(aRequest("Active", null, null));
        UUID deletedId = repository.store(aRequest("Deleted", null, null));
        DatabaseConnection toDelete = repository.findByIdOptional(deletedId).orElseThrow();
        toDelete.delete();
        repository.getEntityManager().flush();
        repository.getEntityManager().clear();

        List<DatabaseConnection> results = repository.findAll().list();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(activeId);
    }

    @Test
    void findAll_emptyTable_returnsEmptyList() {
        List<DatabaseConnection> results = repository.findAll().list();

        assertThat(results).isEmpty();
    }

    private CreateDatabaseConnectionRequest aRequest(String name, String username, String password) {
        CreateDatabaseConnectionRequest request = new CreateDatabaseConnectionRequest();
        request.setName(name);
        request.setType(DatabaseConnectionType.POSTGRES);
        request.setHost("localhost");
        request.setPort(5432L);
        request.setDatabaseName("testdb");
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
