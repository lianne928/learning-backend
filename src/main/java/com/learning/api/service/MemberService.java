package com.learning.api.service;

import com.learning.api.entity.User;
import com.learning.api.repo.MemberRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberRepo memberRepo;

    // check email - findByEmail ( checkEmail != null return email )
    public boolean checkEmail (String email){
        // check null
        if(email==null||email.isEmpty()){
            return false;
        }

        // check rule
        if(!email.contains("@")){
            return false;
        }

        return memberRepo.existsByEmail(email);
    }

    // if repo == null -> register
    public boolean register(User member){
        // check null
        if (member == null || member.getEmail() == null || member.getPassword() == null) {
            return false;
        }

        String email = member.getEmail().trim().toLowerCase();

        //check rule
        if (!(email.contains("@")) || email.isEmpty() || memberRepo.existsByEmail(email)) {
            return false;
        }

        member.setEmail(email);

        // password
        String password = member.getPassword();

        // check null
        if(password == null){
            return false;
        }

        password = password.trim();

        // check rule
        if (password.length()<6){
            return false;
        }

        member.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));

        memberRepo.save(member);

        return true;
    }

    // after register -> login
    public boolean login(User member){
        if (member == null) return false;

        return true;
    }

    public boolean login(String email, String password){
        if (email == null || password == null) return false;

        String rawEmail = email.toLowerCase().trim();
        String rawPassword = password.trim();

        User member = memberRepo.findByEmail(rawEmail).orElse(null);

        if (member == null) return false;

        return BCrypt.checkpw(rawPassword, member.getPassword());
    }

}
