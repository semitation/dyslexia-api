package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.SignUpRequestDto;
import com.dyslexia.dyslexia.dto.SignUpResponseDto;
import com.dyslexia.dyslexia.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = SignUpResponseDto.class))),
    })
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signUp(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "회원가입 요청 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = SignUpRequestDto.class))
        )
        @RequestBody SignUpRequestDto dto) {
        SignUpResponseDto response = userService.signUp(dto);
        return ResponseEntity.ok(response);
    }
}