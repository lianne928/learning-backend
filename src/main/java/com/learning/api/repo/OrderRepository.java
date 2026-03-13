package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.learning.api.entity.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCourseId(Long courseId);
}
