package com.learning.api.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.learning.api.dto.feedback.FeedbackEmailDTO;
import com.learning.api.entity.Booking;
import com.learning.api.entity.Course;
import com.learning.api.entity.LessonFeedback;
import com.learning.api.entity.Order;
import com.learning.api.entity.User;
import com.learning.api.repo.BookingRepo;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.LessonFeedbackRepository;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.UserRepo;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonFeedbackService {

    private static final Logger log = LoggerFactory.getLogger(LessonFeedbackService.class);

    private final LessonFeedbackRepository lessonFeedbackRepository;
    private final BookingRepo bookingRepo;
    private final UserRepo userRepo;
    private final OrderRepository orderRepository;
    private final CourseRepo courseRepo;
    private final EmailService emailService;

    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;
    private static final int MAX_COMMENT_LENGTH = 1000;

    public List<LessonFeedback> findAll() {
        return lessonFeedbackRepository.findAll();
    }

    public Optional<LessonFeedback> findById(Long id) {
        return lessonFeedbackRepository.findById(id);
    }

    public List<LessonFeedback> findByBookingId(Long bookingId) {
        return lessonFeedbackRepository.findByBookingId(bookingId);
    }

    public Double getAverageRating(Long bookingId) {
        List<LessonFeedback> feedbacks = lessonFeedbackRepository.findByBookingId(bookingId);
        if (feedbacks.isEmpty()) return 0.0;

        return feedbacks.stream()
                .mapToDouble(f -> (f.getFocusScore() + f.getComprehensionScore() + f.getConfidenceScore()) / 3.0)
                .average()
                .orElse(0.0);
    }

    public LessonFeedback save(LessonFeedback feedback) {
        validate(feedback);
        LessonFeedback saved = lessonFeedbackRepository.save(feedback);
        buildAndSendFeedbackEmail(saved);
        return saved;
    }

    private void buildAndSendFeedbackEmail(LessonFeedback saved) {
        Booking booking = bookingRepo.findById(saved.getBookingId()).orElse(null);
        if (booking == null) {
            log.warn("Feedback email 略過：找不到 booking，bookingId={}", saved.getBookingId());
            return;
        }

        User student = userRepo.findById(booking.getStudentId()).orElse(null);
        User tutor = userRepo.findById(booking.getTutorId()).orElse(null);
        Order order = orderRepository.findById(booking.getOrderId()).orElse(null);
        if (student == null || tutor == null || order == null) {
            log.warn("Feedback email 略過：缺少相關資料，bookingId={}", booking.getId());
            return;
        }

        Course course = courseRepo.findById(order.getCourseId()).orElse(null);
        if (course == null) {
            log.warn("Feedback email 略過：找不到課程，courseId={}", order.getCourseId());
            return;
        }

        FeedbackEmailDTO dto = new FeedbackEmailDTO();
        dto.setStudentEmail(student.getEmail());
        dto.setStudentName(student.getName());
        dto.setTutorName(tutor.getName());
        dto.setCourseName(course.getName());
        dto.setDate(booking.getDate());
        dto.setHour(booking.getHour());
        dto.setFocusScore(saved.getFocusScore());
        dto.setComprehensionScore(saved.getComprehensionScore());
        dto.setConfidenceScore(saved.getConfidenceScore());
        dto.setComment(saved.getComment() != null ? saved.getComment() : "");

        emailService.sendFeedbackEmail(dto);
    }

    public Optional<LessonFeedback> update(Long id, LessonFeedback updated) {
        return lessonFeedbackRepository.findById(id).map(existing -> {
            validate(updated);
            existing.setFocusScore(updated.getFocusScore());
            existing.setComprehensionScore(updated.getComprehensionScore());
            existing.setConfidenceScore(updated.getConfidenceScore());
            existing.setComment(updated.getComment());
            return lessonFeedbackRepository.save(existing);
        });
    }

    public boolean deleteById(Long id) {
        if (lessonFeedbackRepository.existsById(id)) {
            lessonFeedbackRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void validate(LessonFeedback feedback) {
        validateScore("專注度", feedback.getFocusScore());
        validateScore("理解度", feedback.getComprehensionScore());
        validateScore("自信度", feedback.getConfidenceScore());
        if (feedback.getComment() != null && feedback.getComment().length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("評論不能超過 " + MAX_COMMENT_LENGTH + " 個字元");
        }
    }

    private void validateScore(String fieldName, Integer score) {
        if (score == null) throw new IllegalArgumentException(fieldName + "不能為空");
        if (score < MIN_SCORE || score > MAX_SCORE)
            throw new IllegalArgumentException(fieldName + "必須在 " + MIN_SCORE + " 到 " + MAX_SCORE + " 之間");
    }
}
