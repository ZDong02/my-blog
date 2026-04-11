package com.example.blog.controller;

import com.example.blog.dto.response.ApiResponse;
import com.example.blog.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/captcha")
@CrossOrigin
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * Generate captcha image
     */
    @GetMapping("/generate")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateCaptcha() {
        Map<String, String> result = captchaService.generateCaptcha();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Verify captcha code
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyCaptcha(
            @RequestParam String captchaId,
            @RequestParam String code) {
        boolean valid = captchaService.verifyCaptcha(captchaId, code);
        return ResponseEntity.ok(ApiResponse.success(valid));
    }
}
