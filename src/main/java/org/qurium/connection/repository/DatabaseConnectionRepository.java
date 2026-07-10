/* Qurium - 2026 */
package org.qurium.connection.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.domain.DatabaseConnectionType;
import org.qurium.connection.dto.requests.CreateDatabaseConnectionRequest;

@ApplicationScoped
@RequiredArgsConstructor
public class DatabaseConnectionRepository
        implements PanacheRepositoryBase<DatabaseConnection, UUID> {

    @Transactional(Transactional.TxType.MANDATORY)
    public UUID store(CreateDatabaseConnectionRequest request) {

        DatabaseConnection newConnection = new DatabaseConnection();
        newConnection.setName(request.getName());
        newConnection.setDatabaseName(request.getDatabaseName());
        newConnection.setHost(request.getHost());
        newConnection.setPort(request.getPort());
        newConnection.setType(request.getType());
        newConnection.setUsername(request.getUsername());
        newConnection.setEncryptedPassword(request.getPassword());
        newConnection.setConnectedAt(Instant.now());
        persist(newConnection);

        return newConnection.getId();
    }

    public Optional<DatabaseConnection> findByUniqueDataOptional(
            String databaseName, DatabaseConnectionType type, String host, Long port) {

        return find(
                        "databaseName = ?1 AND type = ?2 AND host = ?3 AND port = ?4",
                        databaseName,
                        type,
                        host,
                        port)
                .firstResultOptional();
    }
}
