package com.learning.api.dto.auth;

/* import com.learning.api.enums.UserRole; */
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

import com.learning.api.enums.UserRole;

@Data
public class RegisterReq {

    @NotBlank(message = "姓名不可為空")
    private String name;

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, message = "密碼至少需要 8 個字元")
    private String password;

    @Past(message = "生日必須是過去的日期")
    private LocalDate birthday;

    // role 改為選填，前端不需要傳送
    // 後端會自動設定為 STUDENT
    @Data
    public static class RegisterReqV2 {

    @NotBlank(message = "姓名不可為空")
    private String name;

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, message = "密碼至少需要 8 個字元")
    private String password;

    @Past(message = "生日必須是過去的日期")
    private LocalDate birthday;

    private UserRole role;
}
}