package com.learning.api.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.api.Spec.CourseSpec;
import com.learning.api.dto.CourseSearchDTO;
import com.learning.api.entity.Course;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.ReviewRepository;
import com.learning.api.repo.TutorScheduleRepo;

@RestController
@CrossOrigin(origins = "*")
public class CourseViewController {

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private TutorScheduleRepo scheduleRepo;

    // 注入 ReviewRepository，用來計算各課程的平均評分
    @Autowired
    private ReviewRepository reviewRepo;

    /**
     * 取得課程列表（含分頁、篩選、平均評分）
     * GET /api/view/courses
     */
    @GetMapping("/api/view/courses")
    public ResponseEntity<Page<CourseSearchDTO>> searchCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) Integer subjectCategory,
            @RequestParam(required = false) Integer subject,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) Integer weekday,
            @RequestParam(required = false) String timeSlot) {

        Pageable pageable = PageRequest.of(page, 10);

        // 1. 執行查詢
        Page<Course> coursePage = courseRepo.findAll(
                CourseSpec.filterCourses(teacherName, courseName, subjectCategory, subject, priceRange, weekday, timeSlot),
                pageable
        );

        // 2. 轉換為 DTO
        Page<CourseSearchDTO> dtoPage = coursePage.map(course -> {

            // 轉換時段格式：TutorSchedule → "1-morning"
            List<String> slots = course.getTutor().getSchedules().stream()
                    .filter(s -> s.getIsAvailable() != null && s.getIsAvailable())
                    .map(s -> {
                        String period = "morning";
                        if (s.getHour() >= 13 && s.getHour() < 17) period = "afternoon";
                        else if (s.getHour() >= 17) period = "evening";
                        return s.getWeekday() + "-" + period;
                    })
                    .collect(Collectors.toList());

            // 計算此課程的平均評分（無評價則回傳 0.0）
            Double avgRating = reviewRepo.findAverageRatingByCourseId(course.getId());
            double rating = (avgRating != null)
                    ? Double.parseDouble(String.format("%.1f", avgRating))
                    : 0.0;

            return new CourseSearchDTO(
                    course.getId(),
                    course.getTutor().getId(),
                    course.getTutor().getUser().getName(),
                    course.getTutor().getAvatar(),
                    course.getTutor().getTitle(),
                    course.getName(),
                    course.getSubject(),
                    course.getDescription(),
                    course.getPrice(),
                    slots,
                    rating  // 平均評分（新增）
            );
        });

        return ResponseEntity.ok(dtoPage);
    }

    /**
     * 取得老師課表
     * GET /api/view/teacher_schedule/{teacherId}
     */
    @GetMapping("/api/view/teacher_schedule/{teacherId}")
    public Map<Integer, List<Integer>> getTeacherSchedule(@PathVariable Long teacherId) {
        List<TutorSchedule> schedules = scheduleRepo.findByTutorId(teacherId);

        return schedules.stream()
                .collect(Collectors.groupingBy(
                        TutorSchedule::getWeekday,
                        Collectors.mapping(TutorSchedule::getHour, Collectors.toList())
                ));
    }
}