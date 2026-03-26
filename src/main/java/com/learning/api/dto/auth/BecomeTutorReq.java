package com.learning.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BecomeTutorReq {

    @NotBlank(message = "職稱不可為空")
    private String title;  // 職稱，例如：「TESL認證英語教師」

    @NotBlank(message = "自我介紹不可為空")
    private String intro;  // 自我介紹

    @NotBlank(message = "最高學歷不可為空")
    private String education;  // 最高學歷

    private String experience1;  // 教學經歷1（選填）
    private String experience2;  // 教學經歷2（選填）

    private String certificateName1;  // 證照名稱1（選填）
    private String certificateName2;  // 證照名稱2（選填）
}