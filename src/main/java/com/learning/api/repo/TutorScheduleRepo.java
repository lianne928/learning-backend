package com.learning.api.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.learning.api.entity.TutorSchedule;

@Repository
public interface TutorScheduleRepo extends JpaRepository<TutorSchedule, Long> {

    List<TutorSchedule> findByTutorIdOrderByWeekdayAscHourAsc(Long tutorId);

    List<TutorSchedule> findByTutorId(Long tutorId);

    Optional<TutorSchedule> findByTutorIdAndWeekdayAndHour(Long tutorId, Integer weekday, Integer hour);
}
