package com.learning.api.service;

import com.learning.api.dto.auth.BecomeTutorReq;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.User;
import com.learning.api.enums.UserRole;
import com.learning.api.repo.TutorRepo;
import com.learning.api.repo.UserRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TutorApplicationService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TutorRepo tutorRepo;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 申請成為老師
     * 1. 檢查使用者是否已經是老師
     * 2. 更新 User 的 role 為 TUTOR
     * 3. 建立 Tutor 記錄並填入基本資料
     */
    @Transactional
    public void becomeTutor(Long userId, BecomeTutorReq req) {
        // 1. 查詢使用者
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));

        // 2. 檢查是否已經是老師
        if (user.getRole() == UserRole.TUTOR) {
            throw new IllegalArgumentException("您已經是老師了");
        }

        // 3. 更新 User 的 role
        user.setRole(UserRole.TUTOR);
        userRepo.save(user);

        // 強制刷新
        entityManager.flush();

        // 4. 建立 Tutor 記錄
        Tutor tutor = new Tutor();
        tutor.setUser(user);
        tutor.setTitle(req.getTitle());
        tutor.setIntro(req.getIntro());
        tutor.setEducation(req.getEducation());
        tutor.setExperience1(req.getExperience1());
        tutor.setExperience2(req.getExperience2());
        tutor.setCertificateName1(req.getCertificateName1());
        tutor.setCertificateName2(req.getCertificateName2());

        entityManager.persist(tutor);
    }
}