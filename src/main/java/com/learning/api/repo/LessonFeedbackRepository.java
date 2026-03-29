package com.learning.api.repo;

import com.learning.api.entity.LessonFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonFeedbackRepository extends JpaRepository<LessonFeedback, Long> {

    boolean existsByBookingId(Long bookingId);

    List<LessonFeedback> findByBookingId(Long bookingId);

    Optional<LessonFeedback> findFirstByBookingId(Long bookingId);
}
