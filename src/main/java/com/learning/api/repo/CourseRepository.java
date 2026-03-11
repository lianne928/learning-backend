package com.learning.api.repo;

import com.learning.api.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    // 找出某個老師所有「已上架」的課程
    List<Course> findByTutorIdAndIsActive(Long tutorId, Integer isActive);
}
