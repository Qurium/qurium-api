/* Qurium - 2026 */
package org.qurium.connection;

import static org.qurium.common.exception.QuriumExceptionCode.DATABASE_CONNECTION_UNREACHABLE;

import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import org.qurium.common.EncryptionService;
import org.qurium.common.exception.QuriumException;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.connection.domain.DatabaseConnectionType;

@ApplicationScoped
@RequiredArgsConstructor
public class DatabaseConnectionFactory {

    private final EncryptionService encryptionService;

    public Connection open(DatabaseConnection connection) {

        String url =
                buildUrl(
                        connection.getType(),
                        connection.getHost(),
                        connection.getPort(),
                        connection.getDatabaseName());

        String password = encryptionService.decrypt(connection.getEncryptedPassword());
        return openJdbc(url, connection.getUsername(), password);
    }

    public Connection open(
            DatabaseConnectionType type,
            String host,
            Long port,
            String databaseName,
            String username,
            String password) {

        String url = buildUrl(type, host, port, databaseName);
        return openJdbc(url, username, password);
    }

    private Connection openJdbc(String url, String username, String password) {

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new QuriumException(DATABASE_CONNECTION_UNREACHABLE);
        }
    }

    public String buildUrl(
            DatabaseConnectionType type, String host, Long port, String databaseName) {

        return switch (type) {
            case POSTGRES -> "jdbc:postgresql://%s:%d/%s".formatted(host, port, databaseName);
            case MYSQL -> "jdbc:mysql://%s:%d/%s".formatted(host, port, databaseName);
            case ORACLE -> "jdbc:oracle:thin:@%s:%d:%s".formatted(host, port, databaseName);
        };
    }
}
