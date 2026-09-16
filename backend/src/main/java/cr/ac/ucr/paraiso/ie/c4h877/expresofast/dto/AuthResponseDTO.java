package cr.ac.ucr.paraiso.ie.c4h877.expresofast.dto;

import java.util.List;

public class AuthResponseDTO {

    private final String token;
    private final String type = "Bearer";
    private final String username;
    private final List<String> roles;
    private final long expirationTime;

    public AuthResponseDTO(String token, String username, List<String> roles, long expirationTime) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.expirationTime = expirationTime;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public long getExpirationTime() {
        return expirationTime;
    }
}
