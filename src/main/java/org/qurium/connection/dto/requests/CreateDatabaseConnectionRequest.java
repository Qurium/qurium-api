/* Qurium - 2026 */
package org.qurium.connection.dto.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.qurium.connection.domain.DatabaseConnectionType;

@Getter
@Setter
public class CreateDatabaseConnectionRequest {

    @NotBlank
    private String name;

    @NotNull private DatabaseConnectionType type;

    @NotBlank private String host;

    @NotNull private Long port;

    @NotBlank private String databaseName;

    private String username;

    private String password;

    @JsonIgnore
    @AssertTrue(message = "host must be a valid hostname or IP address")
    public boolean isHostValid() {
        return host.matches(
                "^(localhost|((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?))$");
    }

    @JsonIgnore
    @AssertTrue(message = "username and password need to be sent together")
    public boolean isUsernameOrPasswordDefined() {
        return (username == null && password == null) || (password != null && username != null);
    }
}
