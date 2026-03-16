package com.learning.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResp {
    private String token;
    private UserResp user;

    public LoginResp(String token) {
        this.token = token;
    }
}
