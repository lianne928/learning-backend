package com.learning.api.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class UserUpdateDTO {
    private String name;        // 姓名
    private LocalDate birthday; // 生日
}