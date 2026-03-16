package com.incredible.lms.dto;

public class AuthenticationResponse {
    private String token;
    private String email;
    private String role;
    private String name;

    public AuthenticationResponse() {
    }

    public AuthenticationResponse(String token, String email, String role, String name) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.name = name;
    }

    public static AuthenticationResponseBuilder builder() {
        return new AuthenticationResponseBuilder();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class AuthenticationResponseBuilder {
        private String token;
        private String email;
        private String role;
        private String name;

        public AuthenticationResponseBuilder token(String token) {
            this.token = token;
            return this;
        }

        public AuthenticationResponseBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AuthenticationResponseBuilder role(String role) {
            this.role = role;
            return this;
        }

        public AuthenticationResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AuthenticationResponse build() {
            return new AuthenticationResponse(token, email, role, name);
        }
    }
}
