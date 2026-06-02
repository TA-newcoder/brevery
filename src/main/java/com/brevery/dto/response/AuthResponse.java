package com.brevery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private String avatarUrl;

    public static AuthResponse of(String accessToken, Long userId,
                                   String email, String fullName,
                                   String role, String avatarUrl) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .fullName(fullName)
                .role(role)
                .avatarUrl(avatarUrl)
                .build();
    }
}
