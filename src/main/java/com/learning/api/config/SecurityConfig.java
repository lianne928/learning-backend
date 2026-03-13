package com.learning.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // filter
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // 取消預設
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))


                // /api/auth
                .authorizeHttpRequests(auth -> auth
                        // 測試 正式上線要刪
<<<<<<< HEAD
                        //.requestMatchers("/api/TestController").permitAll()
=======
                        // .requestMatchers("/api/TestController").permitAll()
                	    .requestMatchers("/test-email/**").permitAll()
>>>>>>> 344655abb502ba63c33f0324a74a7ae71e057652

                        //.requestMatchers("/api/auth/**").permitAll()
                        // teacher
                        //.requestMatchers("/api/teacher/**").hasRole("TEACHER")
                        // student
                        //.requestMatchers("/api/student/**").hasRole("STUDENT")
                        
                        //chat-messages
                        .requestMatchers("/api/chat-messages/**").permitAll()
                        //reviews
                        .requestMatchers("/api/reviews/**").permitAll()
                        //lesson-feedbacks
                        .requestMatchers("/api/lesson-feedbacks/**").permitAll()
                        // WebSocket (SockJS handshake + STOMP)
                        .requestMatchers("/ws/**").permitAll()
                        // 測試用靜態頁面
                        .requestMatchers("/*.html").permitAll()
                        
                        
                        .anyRequest().authenticated()
                );


        return httpSecurity.build();
    }

}
