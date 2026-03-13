package com.learning.api.controller;

import com.learning.api.dto.*;
import com.learning.api.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<?> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        CourseResp resp = courseService.getCourseById(id);
        if (resp == null) return ResponseEntity.status(404).body(Map.of("msg", "課程不存在"));
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<?> sendCourses(@RequestBody CourseReq courseReq){
        if (!courseService.sendCourses(courseReq)) return ResponseEntity.status(400).body(Map.of("msg", "建立失敗"));

        return ResponseEntity.ok(Map.of("msg", "ok"));
    }

}
