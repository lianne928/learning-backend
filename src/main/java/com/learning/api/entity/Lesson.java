package com.learning.api.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;



@Entity
@Table(name = "lessons")
@Getter
@Setter
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "tutor_id", nullable = false)
    private Long tutorId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private Integer hour;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "slot_locked")
    private Boolean slotLocked;
    
    @OneToOne(mappedBy = "lesson")
    private LessonFeedback feedback;
}