package com.learning.api.controller;

import com.learning.api.dto.BookingDTO;
/* import com.learning.api.dto.BookingReq;
import com.learning.api.entity.Booking; */
import com.learning.api.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/* import java.util.Map; */
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<?> getTutorBookings(@PathVariable Long tutorId) {
        List<BookingDTO> bookings = bookingService.getTutorBookings(tutorId);
        return ResponseEntity.ok(bookings);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null) return ResponseEntity.badRequest().body(Map.of("msg", "status 不能為空"));

        return bookingService.updateStatus(id, status)
                ? ResponseEntity.ok(Map.of("msg", "更新成功"))
                : ResponseEntity.notFound().build();
    }
}