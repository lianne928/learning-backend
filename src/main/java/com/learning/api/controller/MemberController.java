package com.learning.api.controller;

import com.learning.api.entity.Member;
import com.learning.api.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Member member){

        if (!memberService.register(member)) {
            return ResponseEntity.status(400).body(Map.of("msg", "註冊失敗"));
        }

        return ResponseEntity.ok(Map.of("msg", "歡迎"));
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Member member){

        if (!memberService.login(member)){
            return ResponseEntity.status(401).body(Map.of("msg", "帳號或密碼錯誤"));
        }

        return ResponseEntity.ok(Map.of("msg", "歡迎"));
    }
}
