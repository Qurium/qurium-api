/* Qurium - 2026 */
package org.qurium.connection.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.dto.requests.CreateDatabaseConnectionRequest;

@ApplicationScoped
@RequiredArgsConstructor
public class DatabaseConnectionRepository
        implements PanacheRepositoryBase<DatabaseConnection, UUID> {

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
}
