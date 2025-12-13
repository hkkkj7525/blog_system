package com.example.blog_system.web.admin;

import com.example.blog_system.model.ResponseData.ArticleResponseData;
import com.example.blog_system.model.domain.User;
import com.example.blog_system.service.impl.OssService;
import com.example.blog_system.service.impl.UserService;
import com.example.blog_system.utils.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final OssService ossService;

    // 使用构造器注入
    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          UserService userService,
                          PasswordEncoder passwordEncoder,
                          OssService ossService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.ossService = ossService;
    }

    // 将LoginRequest移到类开头，定义为静态内部类
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);

            User user = userService.findUserByUsername(loginRequest.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", user);
            response.put("message", "登录成功");

            return ResponseEntity.ok(ArticleResponseData.ok(response));
        } catch (Exception e) {
            logger.error("登录失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ArticleResponseData.fail("用户名或密码错误"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam(required = false) MultipartFile avatar) {

        try {
            // 参数验证
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ArticleResponseData.fail("用户名不能为空"));
            }
            if (password == null || password.length() < 6) {
                return ResponseEntity.badRequest().body(ArticleResponseData.fail("密码长度不能少于6位"));
            }
            if (email == null || !isValidEmail(email)) {
                return ResponseEntity.badRequest().body(ArticleResponseData.fail("邮箱格式不正确"));
            }

            // 检查用户名是否已存在
            if (userService.findUserByUsername(username) != null) {
                return ResponseEntity.badRequest().body(ArticleResponseData.fail("用户名已存在"));
            }

            // 检查邮箱是否已存在
            if (userService.findUserByEmail(email) != null) {
                return ResponseEntity.badRequest().body(ArticleResponseData.fail("邮箱已被注册"));
            }

            // 上传头像
            String avatarUrl = null;
            if (avatar != null && !avatar.isEmpty()) {
                try {
                    avatarUrl = ossService.uploadAvatar(avatar);
                    logger.info("用户头像上传成功: {}", avatarUrl);
                } catch (Exception e) {
                    logger.error("头像上传失败: {}", e.getMessage());
                    return ResponseEntity.badRequest().body(ArticleResponseData.fail("头像上传失败: " + e.getMessage()));
                }
            }

            // 创建新用户
            User user = new User();
            user.setUsername(username.trim());
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email.trim());
            user.setCreated(new Date());
            user.setValid(1); // 默认启用
            user.setImage(avatarUrl); // 设置头像URL

            // 保存用户
            userService.saveUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "注册成功");
            response.put("user", user);

            logger.info("用户注册成功: {}", username);
            return ResponseEntity.ok(ArticleResponseData.ok(response));
        } catch (Exception e) {
            logger.error("注册失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ArticleResponseData.fail("注册失败: " + e.getMessage()));
        }
    }

    /**
     * 邮箱格式验证
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    // 新增：刷新token接口
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(ArticleResponseData.fail("无效的token格式"));
            }

            String token = authorizationHeader.substring(7);

            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.badRequest().body(ArticleResponseData.fail("无效的token"));
            }

            String newToken = jwtTokenProvider.refreshToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("message", "token刷新成功");

            return ResponseEntity.ok(ArticleResponseData.ok(response));
        } catch (Exception e) {
            logger.error("token刷新失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ArticleResponseData.fail("token刷新失败"));
        }
    }

    // 新增：检查token状态
    @GetMapping("/check-token")
    public ResponseEntity<?> checkToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(ArticleResponseData.ok(createTokenStatus(false, "无效的token")));
            }

            String token = authorizationHeader.substring(7);
            boolean isValid = jwtTokenProvider.validateToken(token);
            boolean isExpiringSoon = jwtTokenProvider.isTokenExpiringSoon(token);

            Map<String, Object> status = createTokenStatus(isValid, isValid ? "token有效" : "token无效");
            status.put("expiringSoon", isExpiringSoon);

            if (isValid) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                User user = userService.findUserByUsername(username);
                status.put("user", user);
            }

            return ResponseEntity.ok(ArticleResponseData.ok(status));
        } catch (Exception e) {
            logger.error("检查token状态失败: {}", e.getMessage());
            return ResponseEntity.ok(ArticleResponseData.ok(createTokenStatus(false, "检查token状态失败")));
        }
    }

    private Map<String, Object> createTokenStatus(boolean valid, String message) {
        Map<String, Object> status = new HashMap<>();
        status.put("valid", valid);
        status.put("message", message);
        status.put("checkedAt", new Date());
        return status;
    }
}