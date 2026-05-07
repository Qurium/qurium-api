/* Qurium - 2026 */
package org.qurium.connection.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.connection.domain.DatabaseConnection;

@ApplicationScoped
@RequiredArgsConstructor
public class DatabaseConnectionRepository implements PanacheRepositoryBase<DatabaseConnection, UUID> {}
