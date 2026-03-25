package com.learning.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorUpdateDTO {
    private String avatar;
    private String title;       // 職稱標語
    private String intro;       // 自我介紹
    private String certificate1;
    private String certificateName1;
    private String certificate2;
    private String certificateName2;
    private String videoUrl1;   // 自我介紹影片
    private String videoUrl2;   // 教學示範影片
    private String experience1; // 教學經歷1
    private String experience2; // 教學經歷2
    private String education;   // 最高學歷（新增）
}