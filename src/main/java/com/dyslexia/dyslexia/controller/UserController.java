package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.SignUpRequestDto;
import com.dyslexia.dyslexia.dto.SignUpResponseDto;
import com.dyslexia.dyslexia.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signUp(@RequestBody SignUpRequestDto dto) {

        SignUpResponseDto response = userService.signUp(dto);

        return ResponseEntity.ok(response);
    }
}