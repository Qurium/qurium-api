/* Qurium - 2026 */
package org.qurium.connection.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.connection.domain.DatabaseConnection;
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
        persist(newConnection);

        return newConnection.getId();
    }

    public Optional<DatabaseConnection> findActiveById(UUID id) {

        return find("id = ?1 and deletedAt is null", id).firstResultOptional();
    }

    public List<DatabaseConnection> findAllActive() {

        return find("deletedAt is null").list();
    }
}
