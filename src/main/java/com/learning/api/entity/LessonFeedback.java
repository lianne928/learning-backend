package com.learning.api.entity;
import jakarta.persistence.*;
import lombok.Data;
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

    @OneToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column(nullable = false)
    private Byte rating;

    @Column(nullable = true, length = 1000)
    private String comment;
    
<<<<<<< HEAD

=======
//    @OneToOne
//    @JoinColumn(name = "lesson_id")
//    private Lesson lesson;
>>>>>>> 7354f396dc9c7a39fe91fe05968dd303e0bd21c9
}