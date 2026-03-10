package com.learning.api.repo;

import com.learning.api.entity.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepo extends JpaRepository<Bookings, Long> {

}
