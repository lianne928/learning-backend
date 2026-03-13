package com.learning.api.service;

import com.learning.api.dto.*;
import com.learning.api.entity.*;
import com.learning.api.repo.UserRepository;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.BookingRepository;
import com.learning.api.repo.LessonFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private LessonFeedbackRepository feedbackRepo;

    // bookingReq.getUserId() -> 這是前端送 id
    public boolean sendCourses(CourseReq courseReq){

        if (courseReq == null) {
            System.out.println("courseReq is null");
            return false;
        }

        // check null
        if (courseReq.getTutorId() == null || courseReq.getName() == null ||
            courseReq.getSubject() == null  || courseReq.getLevel() == null ||
                courseReq.getPrice() == null || courseReq.getActive() == null) return false;

        if (courseReq.getName().trim().isEmpty()) {
            System.out.println("name is empty");
            return false;
        }

        // 有要設定這堂課不開就不能 post 嗎？ (暫時先：對）
        // if (!courseReq.isActive()) return false;

        // 先設定 1 塊就可開課 要改最低多少再改
        if (courseReq.getPrice() <= 0) {
            System.out.println("price is wrong");
            return false;
        }

        // 目前只有 1 英文科 2 程式語言科 不能 0 負數
        if (courseReq.getSubject() <= 0) return false;
        if (courseReq.getSubject()!=1 || courseReq.getSubject()!=2) return false;

        // level 1-5 不能 0 負數
        if (courseReq.getLevel() <= 0) return false;
        if (courseReq.getLevel() <1 || courseReq.getLevel() >5) return false;

        // member existsById
        User tutor = userRepo.findById(courseReq.getTutorId()).orElse(null);
        if ( tutor == null ) {
            System.out.println("tutor is null");
            return false;
        }

        // 只有老師可以新增課程
        if (tutor.getRole() != 2) {
            System.out.println("user isn't tutor");
            return false;
        }

        // buildCourses
        Course course = buildCourses(courseReq);
        courseRepo.save(course);

        // member existsById
        return true;
    }

    public List<CourseResp> getAllCourses() {
        return courseRepo.findAll().stream()
                .map(this::buildCourseResp)
                .collect(Collectors.toList());
    }

    public CourseResp getCourseById(Long courseId) {
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course == null) return null;
        return buildCourseResp(course);
    }

    private CourseResp buildCourseResp(Course course) {
        List<Long> orderIds = orderRepo.findByCourseId(course.getId()).stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        List<Long> bookingIds = orderIds.isEmpty()
                ? List.of()
                : bookingRepo.findByOrderIdIn(orderIds).stream()
                        .map(Bookings::getId)
                        .collect(Collectors.toList());

        List<LessonFeedback> feedbacks = bookingIds.isEmpty()
                ? List.of()
                : feedbackRepo.findByBookingIdIn(bookingIds);

        Double avgRating = bookingIds.isEmpty()
                ? null
                : feedbackRepo.findAverageRatingByBookingIdIn(bookingIds);

        CourseResp resp = new CourseResp();
        resp.setId(course.getId());
        resp.setTutorId(course.getTutorId());
        resp.setName(course.getName());
        resp.setSubject(course.getSubject());
        resp.setLevel(course.getLevel());
        resp.setDescription(course.getDescription());
        resp.setPrice(course.getPrice());
        resp.setActive(course.getActive());
        resp.setAvgRating(avgRating);
        resp.setFeedbacks(feedbacks.stream()
                .map(f -> new CourseResp.FeedbackItem(f.getRating(), f.getComment()))
                .collect(Collectors.toList()));
        return resp;
    }

    private Course buildCourses(CourseReq courseReq){

        Course course = new Course();
        //set
        course.setTutorId(courseReq.getTutorId());
        course.setName(courseReq.getName().trim());
        course.setSubject(courseReq.getSubject());
        course.setLevel(courseReq.getLevel());
        course.setDescription(courseReq.getDescription());
        course.setPrice(courseReq.getPrice());
        course.setActive(courseReq.getActive());

        return course;
    }
}
