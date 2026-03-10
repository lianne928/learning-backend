package com.learning.api.repo;

import com.learning.api.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
import java.util.List;

public interface CourseRepo extends JpaRepository<Course, Long> {
    // 找出某個老師所有「已上架」的課程
    List<Course> findByTutorIdAndIsActive(Long tutorId, Integer isActive);
}
=======
public interface CourseRepo extends JpaRepository<Course, Long> {
}
>>>>>>> fdd70f84c76e7adb8da930e439252dc5e691a8a4
