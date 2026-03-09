package com.learning.api.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<<<<<<< HEAD
    @Column(name = "user_id", nullable = false)
    private Long userId;
=======
    // 先 true
    @Column(name = "order_id", nullable = true)
    private Long orderId;
>>>>>>> 7354f396dc9c7a39fe91fe05968dd303e0bd21c9

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "discount_price", nullable = false)
    private Integer discountPrice;

    @Column(name = "lesson_count", nullable = false)
    private Integer lessonCount;

    @Column(name = "lesson_used", nullable = false)
    private Integer lessonUsed;
    
    @Column(name = "status", nullable = false)
    private Integer status;
}