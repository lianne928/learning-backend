package com.learning.api.service;

import com.learning.api.dto.auth.*;
import com.learning.api.entity.*;
import com.learning.api.enums.UserRole;
import com.learning.api.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    @Autowired
    private UserRepo memberRepo;

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
}
