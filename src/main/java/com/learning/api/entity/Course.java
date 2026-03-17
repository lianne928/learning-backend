package com.learning.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "courses")
@Getter
@Setter
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tutor_id", nullable = false)
    private Long tutorId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private Integer subject;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer price;

    // 👉 修正：對齊 DB 的 is_active，並建議使用 Boolean 物件
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}