package com.learning.api.service;

import com.learning.api.dto.auth.*;
import com.learning.api.dto.auth.RegisterReq.RegisterReqV2;
import com.learning.api.entity.*;
import com.learning.api.enums.UserRole;
import com.learning.api.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* import java.time.LocalDate; */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {
    private static final Logger log =
            LoggerFactory.getLogger(MemberService.class);


    @Autowired
    private UserRepo memberRepo;

    @Autowired
    private TutorRepo tutorRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 註冊新會員 - 所有人都先註冊為 STUDENT
     */
    @Transactional
    public void register(RegisterReq registerReq) {
        String email = registerReq.getEmail().trim().toLowerCase();

        // 檢查 Email 是否已存在
        if (memberRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("此 email 已被註冊");
        }

        // 使用 PasswordEncoder 加密密碼
        String hashPassword = passwordEncoder.encode(registerReq.getPassword());

        // 建立 User - 所有人都先註冊為 STUDENT
        User user = new User();
        user.setName(registerReq.getName());
        user.setEmail(email);
        user.setPassword(hashPassword);
        user.setBirthday(registerReq.getBirthday());
        user.setRole(UserRole.STUDENT);  // ← 固定為 STUDENT
        user.setWallet(0);

        memberRepo.save(user);
    }

   @Transactional
    public void register(RegisterReqV2 registerReq) {
        String email = registerReq.getEmail().trim().toLowerCase();

        if (memberRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("此 email 已被註冊");
        }

        String hashPassword = passwordEncoder.encode(registerReq.getPassword());

    
        // 1. 建立 User 實體
        User user = new User();
        user.setName(registerReq.getName());
        user.setEmail(email);
        user.setPassword(hashPassword);
        user.setBirthday(registerReq.getBirthday());
        
        // 判斷角色：若前端有傳且為 TUTOR 則設定，否則預設為 STUDENT
        UserRole targetRole = (registerReq.getRole() == UserRole.TUTOR) ? UserRole.TUTOR : UserRole.STUDENT;
        log.info("Target Role: {}", targetRole);
        user.setRole(targetRole);
        user.setWallet(0);

        // 儲存 User 以取得 ID
        User savedUser = memberRepo.save(user);

        // 2. 如果是 TUTOR，同步存入 tutors 表
        if (targetRole == UserRole.TUTOR) {
            Tutor tutor = new Tutor();
            tutor.setUser(savedUser); // 建立 OneToOne 關聯
            tutor.setStatus(1); // 預設審核中狀態
            
            tutorRepo.save(tutor);
        }
    }
}
