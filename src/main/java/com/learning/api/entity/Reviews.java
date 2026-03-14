package com.learning.api.entity;
<<<<<<< HEAD
<<<<<<<< HEAD:src/main/java/com/learning/api/entity/Reviews.java
========
import java.time.Instant;
>>>>>>>> upstream/feature/Review:src/main/java/com/learning/api/entity/Review.java
=======
>>>>>>> upstream/feature/Review

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "reviews")
@Getter
@Setter
public class Reviews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "focus_score", nullable = false)
    private Integer focusScore;

    @Column(name = "comprehension_score", nullable = false)
    private Integer comprehensionScore;

    @Column(name = "confidence_score", nullable = false)
    private Integer confidenceScore;

    @Column(nullable = true, length = 1000)
    private String comment;

<<<<<<< HEAD
/*     @Column(name = "updated_at", nullable = true,  insertable = false, updatable = false)
=======
/*     @Column(nullable = true,  insertable = false, updatable = false)
>>>>>>> upstream/feature/Review
    private Instant updatedAt; */
}