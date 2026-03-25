package com.learning.api.controller;

import com.learning.api.dto.UserUpdateDTO;
import com.learning.api.dto.auth.UserResp;
import com.learning.api.entity.User;
import com.learning.api.entity.WalletLog;
import com.learning.api.repo.UserRepo;
import com.learning.api.security.SecurityUser;
import com.learning.api.service.WalletLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users")
public class MeController {

    // 注入錢包流水帳 Service（查詢目前登入使用者的所有錢包變動記錄）
    @Autowired
    private WalletLogsService walletLogsService;
    //修改資料使用
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 取得目前登入使用者的個人資料
     * GET /api/users/me
     * 需要帶 JWT Token，從 @AuthenticationPrincipal 取得目前登入的使用者
     * 回傳：userId、name、email、birthday、role、wallet 餘額、建立時間、更新時間
     */
    @GetMapping("/me")
    public ResponseEntity<UserResp> getMe(@AuthenticationPrincipal SecurityUser securityUser) {
        User user = securityUser.getUser();
        UserResp resp = new UserResp(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                user.getRole(),
                user.getWallet(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
        return ResponseEntity.ok(resp);
    }

    /**
     * 取得目前登入使用者的錢包流水帳
     * GET /api/users/wallet-logs
     * 需要帶 JWT Token
     * 回傳：所有錢包變動記錄，依時間倒序排列
     * transactionType：1=儲值、2=購課扣款、3=授課收入
     */
    @GetMapping("/wallet-logs")
    public ResponseEntity<List<WalletLog>> getWalletLogs(@AuthenticationPrincipal SecurityUser me) {
        List<WalletLog> logs = walletLogsService.getLogsByUserId(me.getUser().getId());
        return ResponseEntity.ok(logs);
    }

    /**
     * 修改目前登入使用者的基本資料（姓名、生日）
     * PUT /api/users/me
     * 需要帶 JWT Token
     * email 不開放修改（登入帳號）
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateMe(
            @AuthenticationPrincipal SecurityUser me,
            @RequestBody UserUpdateDTO dto) {
        User user = me.getUser();
        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getBirthday() != null) user.setBirthday(dto.getBirthday());
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("msg", "個人資料已更新"));
    }

    /**
     * 修改目前登入使用者的密碼
     * PUT /api/users/me/password
     * 需要帶 JWT Token
     * 需傳入：oldPassword（舊密碼驗證）、newPassword（新密碼）
     */
    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal SecurityUser me,
            @RequestBody Map<String, String> body) {

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("msg", "舊密碼與新密碼不能為空"));
        }
        if (newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("msg", "新密碼至少需要 8 個字元"));
        }

        User user = me.getUser();

        // 驗證舊密碼是否正確
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(400).body(Map.of("msg", "舊密碼不正確"));
        }

        // 加密新密碼後儲存
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("msg", "密碼已更新"));
    }
}