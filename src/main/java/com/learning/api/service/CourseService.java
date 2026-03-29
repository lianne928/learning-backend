package com.learning.api.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learning.api.dto.CourseDto;
import com.learning.api.dto.CourseReq;
import com.learning.api.dto.CourseSearchDTO;
import com.learning.api.entity.Course;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.TutorRepo;

@Service
public class CourseService {

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private TutorRepo tutorRepo;

    // ── 查：所有課程 ──────────────────────────────────────────────────

    public List<CourseDto> getCoursesByTutorId(Long tutorId) {
        validateTutorExists(tutorId);
        return courseRepo.findByTutorId(tutorId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── 查：單一課程 ──────────────────────────────────────────────────

    public CourseDto getCourse(Long tutorId, Long courseId) {
        Course course = findCourseOrThrow(courseId);
        validateCourseOwnership(course, tutorId);
        return toDTO(course);
    }

    // ── 增 ────────────────────────────────────────────────────────────

    @Transactional
    public CourseDto createCourse(Long tutorId, CourseReq dto) {
        Tutor tutor = tutorRepo.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("找不到老師 id=" + tutorId));

        Course course = new Course();
        course.setTutor(tutor);
        course.setName(dto.getName());
        course.setSubject(dto.getSubject());
        course.setDescription(dto.getDescription());
        course.setPrice(dto.getPrice());
        course.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        return toDTO(courseRepo.save(course));
    }

    // ── 修 ────────────────────────────────────────────────────────────

    @Transactional
    public CourseDto updateCourse(Long tutorId, Long courseId, CourseReq dto) {
        Course course = findCourseOrThrow(courseId);
        validateCourseOwnership(course, tutorId);

        if (dto.getName()        != null) course.setName(dto.getName());
        if (dto.getSubject()     != null) course.setSubject(dto.getSubject());
        if (dto.getDescription() != null) course.setDescription(dto.getDescription());
        if (dto.getPrice()       != null) course.setPrice(dto.getPrice());
        if (dto.getIsActive()    != null) course.setIsActive(dto.getIsActive());

        return toDTO(courseRepo.save(course));
    }

    // ── 刪 ────────────────────────────────────────────────────────────

    @Transactional
    public void deleteCourse(Long tutorId, Long courseId) {
        Course course = findCourseOrThrow(courseId);
        validateCourseOwnership(course, tutorId);
        courseRepo.delete(course);
    }

    // ── 其他查詢（供其他 Controller/Service 使用）────────────────────

    public Optional<Course> findById(Long id) {
        return courseRepo.findById(id);
    }

    public List<Course> findByTutorId(Long tutorId) {
        return courseRepo.findByTutorId(tutorId);
    }

    public List<Course> findByTutorIdActive(Long tutorId) {
        return courseRepo.findByTutorId(tutorId).stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .toList();
    }

    public boolean deleteById(Long id) {
        if (courseRepo.existsById(id)) {
            courseRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public List<CourseSearchDTO> getAllCourseCards() {
        List<Course> courses = courseRepo.findAll().stream()
                .filter(c -> c.getIsActive() != null && c.getIsActive())
                .collect(Collectors.toList());

        return courses.stream().map(course -> {
            CourseSearchDTO dto = new CourseSearchDTO();
            dto.setId(course.getId());
            dto.setTutorId(course.getTutor().getId());
            dto.setTeacherName(course.getTutor().getUser().getName());
            dto.setAvatarUrl(course.getTutor().getAvatar());
            dto.setTitle(course.getTutor().getTitle());
            dto.setCourseName(course.getName());
            dto.setSubject(course.getSubject());
            dto.setDescription(course.getDescription());
            dto.setPrice(course.getPrice());

            if (course.getTutor().getSchedules() != null) {
                List<String> slots = course.getTutor().getSchedules().stream()
                        .filter(TutorSchedule::getIsAvailable)
                        .map(this::convertToSlotTag)
                        .collect(Collectors.toList());
                dto.setAvailableSlots(slots);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    // ── 私有輔助方法 ──────────────────────────────────────────────────

    private CourseDto toDTO(Course course) {
        return new CourseDto(
            course.getId(),
            course.getName(),
            course.getSubject(),
            course.getDescription(),
            course.getPrice(),
            course.getIsActive()
        );
    }

    private void validateTutorExists(Long tutorId) {
        if (!tutorRepo.existsById(tutorId)) {
            throw new RuntimeException("找不到老師 id=" + tutorId);
        }
    }

    private Course findCourseOrThrow(Long courseId) {
        return courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("找不到課程 id=" + courseId));
    }

    private void validateCourseOwnership(Course course, Long tutorId) {
        if (!course.getTutor().getId().equals(tutorId)) {
            throw new SecurityException("此課程不屬於老師 id=" + tutorId);
        }
    }

    private String convertToSlotTag(TutorSchedule s) {
        String period = "morning";
        int hour = s.getHour();
        if (hour >= 13 && hour < 17) period = "afternoon";
        else if (hour >= 17) period = "evening";
        return s.getWeekday() + "-" + period;
    }
}
