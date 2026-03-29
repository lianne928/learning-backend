package com.learning.api.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tutors")
@Getter  // ⚠️ 這個很重要！
@Setter  // ⚠️ 這個很重要！
public class Tutor {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // ⚠️ 申請日期（必須加上！）
    @Column(name = "apply_date")
    private LocalDate applyDate;

    @Column(length = 500)
    private String avatar;

    @Column(length = 50)
    private String title;

    @Column(name = "intro",length = 1000)
    private String intro;

    @Column(name = "certificate_1", length = 500)
    private String certificate1;

    @Column(name = "certificate_name_1", length = 40)
    private String certificateName1;

    @Column(name = "certificate_2", length = 500)
    private String certificate2;

    @Column(name = "certificate_name_2", length = 500)
    private String certificateName2;

    @Column(name = "video_url_1", length = 500)
    private String videoUrl1;

    @Column(name = "video_url_2", length = 500)
    private String videoUrl2;

    @Column(name = "bank_code", length = 10)
    private String bankCode;

    @Column(name = "bank_account", length = 20)
    private String bankAccount;

    // ⚠️ 審核狀態（必須加上！）
    @Column(name = "status")
    private Integer status = 1;

    @Column(name = "experience_1", length = 200)
    private String experience1;

    @Column(name = "experience_2", length = 200)
    private String experience2;

    @Column(name = "education", length = 100)
    private String education;

    @OneToMany(mappedBy = "tutor")
    private List<TutorSchedule> schedules;
}