package com.learning.api.entity;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Byte rating;

    @Column(nullable = true, length = 1000)
    private String comment;
}
