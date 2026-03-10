package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.learning.api.entity.Order;

public interface BookingRepository extends JpaRepository<Order, Long> {
}
