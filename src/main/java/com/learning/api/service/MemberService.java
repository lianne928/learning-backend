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

    /* public User buildMember(RegisterReq registerReq, String email, String hashPassword){
        User newMember = new User();
        newMember.setName(registerReq.getName());
        newMember.setEmail(email);
        newMember.setPassword(hashPassword);
        newMember.setBirthday(registerReq.getBirthday());
        newMember.setRole(registerReq.getRole());
        newMember.setWallet(0);
        return newMember;
    }

    // login
    public LoginResp login(LoginReq loginReq) {
        String rawEmail = loginReq.getEmail().trim().toLowerCase();
        //String rawPassword = loginReq.getPassword().trim();
        String rawPassword = loginReq.getPassword();

        User user = memberRepo.findByEmail(rawEmail).orElse(null);

        if (user == null) throw new IllegalArgumentException("你沒有註冊喔");

        if (!BCrypt.checkpw(rawPassword, user.getPassword())) throw new IllegalArgumentException("密碼錯誤");

        // token JwtService
        String token = jwtService.generateToken(user); */

        /* String token = jwtService.generateToken(user); */
 
/*         UserResp userResp = new UserResp(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                user.getRole(),
                user.getWallet(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        ); */

/* 
        return new LoginResp(token);
    } */
}

