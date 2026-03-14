package com.learning.api.repo;

import com.learning.api.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
=======

>>>>>>> upstream/feature/Review
import java.util.List;
public interface CourseRepo extends JpaRepository<Course, Long> {
<<<<<<< HEAD
    // 找出某個老師所有「已上架」的課程
    List<Course> findByTutorIdAndActive(Long tutorId, boolean active);
    boolean existsByTutorId(Long tutorId);
    // 找出某個老師所有課程（不分上下架）
=======
    boolean existsByTutorId(Long tutorId);
>>>>>>> upstream/feature/Review
    List<Course> findByTutorId(Long tutorId);
}
