package com.learning.api.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tutors")
@Getter
@Setter
public class Tutor {
    @Id
    private Long id;

    @Column(name="apply_date")
    private LocalDate applyDate;
    
    @Column(length = 50)
    private String title;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl; //大頭照url

    @Column(name = "intro",length = 1000)
    private String intro;

    @Column(name = "education", length = 100)
    private String education; // 最高學歷

    @Column(name ="certificate_1" ,length = 500)
    private String certificate1;//位址

    @Column(name ="certificate_name_1" ,length = 500)
    private String certificateName1;//證照名稱

    @Column(name ="certificate_2" ,length = 500)
    private String certificate2;//位址
    
    @Column(name ="certificate_name_2" ,length = 500)
    private String certificateName2;//證照名稱

    @Column(name = "video_url_1", length = 500)
    private String videoUrl1; //自我介紹影片url

    @Column(name = "video_url_2", length = 500)
    private String videoUrl2;//試教介紹影片url

    @Column(name = "bank_code", length = 10)
    private String bankCode;

    @Column(name = "bank_account", length = 20)
    private String bankAccount;

    @Column(name="status")
    private Integer status;//1 pending 2qualified 3停權
}