/* Qurium - 2026 */
package org.qurium.connection.dto.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateDatabaseConnectionRequest {

    @NotBlank private String name;
    @NotBlank private String host;
    @NotNull private Long port;
    @NotBlank private String databaseName;
    private String username;
    private String password;

    @JsonIgnore
    @AssertTrue(message = "Host must be a valid hostname or IP address.")
    public boolean isHostValid() {
        if (host == null || host.isBlank()) return true;
        return host.matches(
                "^(localhost|((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?))$");
    }

    @JsonIgnore
    @AssertTrue(message = "Username and password need to be sent together.")
    public boolean isUsernameOrPasswordDefined() {
        return (username == null && password == null) || (password != null && username != null);
    }
}
