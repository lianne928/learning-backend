package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.learning.api.entity.Bookings;

public interface BookingRepository extends JpaRepository<Bookings, Long> {
}
