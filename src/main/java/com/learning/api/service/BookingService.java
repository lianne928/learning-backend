package com.learning.api.service;

import com.learning.api.dto.*;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private MemberRepo memberRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
<<<<<<< HEAD
    private OrderRepository orderRepo;
=======
    private BookingRepository bookingRepo;

    @Autowired
    private OrderRepo orderRepo;
>>>>>>> 873d81eeb5bae0eb2a6d9f604e4a0f533f7b70df

<<<<<<< HEAD
    // 之後 JWT 做完 改掉 OrderReq.getUserId() -> 這是前端送 id
=======
    // 之後 JWT 做完 改掉 bookingReq.getUserId() -> 這是前端送 id
>>>>>>> 643af9dabe403cacc2abbf7617721c53ea9592a1
    public boolean sendBooking(BookingReq bookingReq){

        if (bookingReq == null) return false;

        // check null
        if (bookingReq.getUserId() == null || bookingReq.getCourseId() == null || bookingReq.getLessonCount() == null) return false;

        // lessonCount > 0
        if (bookingReq.getLessonCount() <= 0) return false;

        // member existsById
        if(!memberRepo.existsById(bookingReq.getUserId())) return false;

        // course findById
        Course course = courseRepo.findById(bookingReq.getCourseId()).orElse(null);
        if (course == null) return false;

        // check courseId isActive
        if (!course.isActive()) return false;

        // buildBooking
        Bookings booking = buildBooking(bookingReq, course);
        bookingRepo.save(booking);

        return true;
    }

    private Order buildOrder(BookingReq bookingReq, Course course){
        Order order = new Order();

<<<<<<< HEAD
        order.setUserId(bookingReq.getUserId());
        order.setCourseId(bookingReq.getCourseId());

        // price unitPrice discountPrice
        Integer originalPrice = course.getPrice();
        Integer discount = afterDiscPrice(originalPrice, bookingReq.getLessonCount());

        order.setUnitPrice(originalPrice);
        order.setDiscountPrice(discount);

        // lessonCount
        order.setLessonCount(bookingReq.getLessonCount());
        order.setLessonused(0);
        // status first send -> 1
        order.setStatus((byte) 1);

        return order;
=======
        // Create Order
        Order order = new Order();
        order.setUserId(bookingReq.getUserId());
        order.setCourseId(bookingReq.getCourseId());
        
        // Price calculation
        Integer originalPrice = course.getPrice();
        Integer discountPrice = afterDiscPrice(originalPrice, bookingReq.getLessonCount());
        
        order.setUnitPrice(originalPrice);
        order.setDiscountPrice(discountPrice);
        order.setLessonCount(bookingReq.getLessonCount());
        order.setStatus(1);

        // Save order and set its id to booking
        Order savedOrder = orderRepo.save(order);
        booking.setOrderId(savedOrder.getId());
        
        return booking;
>>>>>>> 873d81eeb5bae0eb2a6d9f604e4a0f533f7b70df
    }

    private Integer afterDiscPrice(Integer originalPrice, Integer lessonCount){
        // 95% 10 堂
        if (lessonCount >= 10){
            return ((int) (originalPrice*0.95));
        }

        // 0%
        return originalPrice;
    }
}