package logger.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserPrincipal {
    private final String userId;
    private final String username;
    private final String email;
    private final List<String> roles;
    private final String tenantId;

    public UserPrincipal(String userId, String username, String email, List<String> roles, String tenantId) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roles = roles != null ? new ArrayList<>(roles) : Collections.emptyList();
        this.tenantId = tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", roles=" + roles +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }
}
