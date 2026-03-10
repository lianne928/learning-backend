package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.learning.api.entity.Tutor;

public interface TutorRepository extends JpaRepository<Tutor, Long> {
}
