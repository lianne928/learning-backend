package com.learning.api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.api.dto.BookingResponseDTO;
/* import com.learning.api.dto.BuycourseRequestDTO; */
import com.learning.api.dto.CancelBookingRequestDTO;
import com.learning.api.dto.CancelResponseDTO;
import com.learning.api.dto.CourseResponseDto;
import com.learning.api.dto.PackageResponseDTO;
import com.learning.api.dto.RefundOrderRequestDTO;
import com.learning.api.dto.TodayCourseDto;
import com.learning.api.entity.User;
import com.learning.api.repo.UserRepo;
import com.learning.api.security.JwtService;
import com.learning.api.service.StudentCourseService;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api")
public class StudentCourseController {
	
	@Autowired
	private JwtService jwtService;
	
	@Autowired
	private UserRepo userRepo;

    @Autowired
    private StudentCourseService courseService;
    
    private User getJWT(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String email = jwtService.email(token);
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
    // 4 & 6. 取得我的所有課程包 (Student Package / My Courses)
    @GetMapping({"/student-packages/me", "/orders/me"})
    public List<PackageResponseDTO> getMyPackages(HttpServletRequest request) {
    	User user = getJWT(request);
        return courseService.getMyPackages(user.getId());
    }

    // 4. 取得特定課程包資訊
    @GetMapping("/student-packages/{packageId}")
    public PackageResponseDTO getPackageDetail(@PathVariable Long packageId) {
        return courseService.getPackageById(packageId);
    }

    // 6. 取得我的預約紀錄 (My Bookings)
    @GetMapping("/courses/me")
    public List<BookingResponseDTO> getMycourses(HttpServletRequest request) {
    	User user = getJWT(request);
        return courseService.getMyCourses(user.getId());
    }
    
    @GetMapping("/bookings/me")
    public List<BookingResponseDTO> getMyBookings(@RequestParam("orderId") Long orderId) {
        return courseService.getBookingsByOrder(orderId);
    }
    
    @GetMapping("/today/me")
    public List<TodayCourseDto> getMyTodayCourses(HttpServletRequest request) {
    	User user = getJWT(request);
        return courseService.getTodayCourses(user.getId());
    }
    
    @GetMapping("/daily/me")
    public List<CourseResponseDto> getMyCoursesByDate(
    		HttpServletRequest request,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    	User user = getJWT(request);
        return courseService.getCoursesByDate(user.getId(), date);
    }
    
    @GetMapping("/future/me")
    public List<TodayCourseDto> getMyFutureCourses(HttpServletRequest request) {
    	User user = getJWT(request);
        return courseService.getFutureCourses(user.getId());
    }
    
    @PostMapping("/bookings/cancel")
    public CancelResponseDTO cancelBooking(@RequestParam("bookingId")Long bookingId,HttpServletRequest request) {
    	User user = getJWT(request);
        return courseService.cancelBooking(bookingId, user.getId());
    }

    @PostMapping("/orders/refund")
    public String refundOrder(@RequestParam("orderId") Long orderId,HttpServletRequest request) {
    	User user = getJWT(request);
        return courseService.refundEntireOrder(orderId, user.getId());
    }

//    // 新增購買課程的 API
//    @PostMapping("/orders/buycourse")
//    public PackageResponseDTO buycourseCourse(@RequestBody BuycourseRequestDTO request) {
//        return courseService.Buycourse(request.userId(), request.courseId(), request.lessonCount());
//    }

}
