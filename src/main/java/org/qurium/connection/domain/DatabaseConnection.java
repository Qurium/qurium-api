/* Qurium - 2026 */
package org.qurium.connection.domain;

import jakarta.persistence.*;
import jakarta.persistence.EnumType;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.qurium.common.BaseEntity;

@Entity
@Table(name = "database_connection")
@Getter
@Setter
@NoArgsConstructor
public class DatabaseConnection extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DatabaseConnectionType type;

    @Column(name = "host", nullable = false)
    private String host;

    @Column(name = "port", nullable = false)
    private Long port;

    @Column(name = "database_name", nullable = false)
    private String databaseName;

    @Column(name = "username")
    private String username;

    @Column(name = "encrypted_password")
    private String encryptedPassword;

    @Column(name = "connected_at")
    private Instant connectedAt;
}
