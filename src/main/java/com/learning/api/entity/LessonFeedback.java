package com.learning.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lesson_feedback")
@Getter
@Setter
public class LessonFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private Byte rating;

    @Column(nullable = true, length = 1000)
    private String comment;
<<<<<<< HEAD

    //@OneToOne
    //@JoinColumn(name = "lesson_id")
    //private Lesson lesson;
=======
    
//    @OneToOne
//    @JoinColumn(name = "lesson_id")
//    private Lesson lesson;
>>>>>>> fdd70f84c76e7adb8da930e439252dc5e691a8a4
}