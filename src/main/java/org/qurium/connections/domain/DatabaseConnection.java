package org.qurium.connections.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "database_connection")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class DatabaseConnection extends PanacheEntityBase {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private DatabaseConnectionType type;

    @Column(name = "host")
    private String host;

    @Column(name = "port")
    private Long port;

    @Column(name = "database_name")
    private String databaseName;

    @Column(name = "username")
    private String username;

    @Column(name = "encrypted_password")
    private String encryptedPassword;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt =  Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
