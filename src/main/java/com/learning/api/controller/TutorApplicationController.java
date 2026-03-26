package com.learning.api.controller;

import com.learning.api.dto.auth.BecomeTutorReq;
import com.learning.api.security.JwtService;
import com.learning.api.service.TutorApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/tutor")
public class TutorApplicationController {

    @Autowired
    private TutorApplicationService tutorApplicationService;

    @Autowired
    private JwtService jwtService;

    /**
     * POST /api/tutor/become
     * 申請成為老師
     */
    @PostMapping("/become")
    public ResponseEntity<?> becomeTutor(
            @Valid @RequestBody BecomeTutorReq req,
            HttpServletRequest request
    ) {
        // 從 JWT 取得 userId
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtService.userId(token);

        tutorApplicationService.becomeTutor(userId, req);

        return ResponseEntity.ok().body(Map.of("msg", "成為老師申請成功！"));
    }
}