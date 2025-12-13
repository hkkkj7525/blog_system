package com.example.blog_system.web.admin;

import com.example.blog_system.dao.UserMapper;
import com.example.blog_system.dao.UserStatistics;
import com.example.blog_system.model.ResponseData.ArticleResponseData;
import com.example.blog_system.model.domain.User;
import com.example.blog_system.service.impl.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    private static final Logger logger = LoggerFactory.getLogger(UserAdminController.class);

    private final UserService userService;
    private final UserMapper userMapper;

    // 使用构造器注入替代字段注入
    @Autowired
    public UserAdminController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    // 用户管理页面
    @GetMapping("")
    public String userManagement(HttpServletRequest request) {
        List<User> users = userService.findAllUsers();
        UserStatistics statistics = userService.getUserStatistics();

        request.setAttribute("users", users);
        request.setAttribute("statistics", statistics);
        return "back/user_management";
    }

    // 搜索用户
    @GetMapping("/search")
    @ResponseBody
    public ArticleResponseData<?> searchUsers(@RequestParam String keyword) {
        try {
            List<User> users = userService.searchUsers(keyword);
            Map<String, Object> data = new HashMap<>();
            data.put("users", users);
            data.put("count", users.size());
            return ArticleResponseData.ok(data);
        } catch (Exception e) {
            logger.error("搜索用户失败，错误信息: {}", e.getMessage());
            return ArticleResponseData.fail("搜索用户失败");
        }
    }

    // 启用用户
    @PostMapping("/enable")
    @ResponseBody
    public ArticleResponseData<String> enableUser(@RequestParam Integer userId) {
        try {
            userService.updateUserStatus(userId, true);
            logger.info("启用用户成功，用户ID: {}", userId);
            return ArticleResponseData.ok("用户启用成功");
        } catch (Exception e) {
            logger.error("启用用户失败，错误信息: {}", e.getMessage());
            return ArticleResponseData.fail("启用用户失败");
        }
    }

    // 禁用用户
    @PostMapping("/disable")
    @ResponseBody
    public ArticleResponseData<String> disableUser(@RequestParam Integer userId) {
        try {
            userService.updateUserStatus(userId, false);
            logger.info("禁用用户成功，用户ID: {}", userId);
            return ArticleResponseData.ok("用户禁用成功");
        } catch (Exception e) {
            logger.error("禁用用户失败，错误信息: {}", e.getMessage());
            return ArticleResponseData.fail("禁用用户失败");
        }
    }

    // 重置用户密码
    @PostMapping("/reset-password")
    @ResponseBody
    public ArticleResponseData<String> resetUserPassword(@RequestParam Integer userId,
                                                         @RequestParam String newPassword) {
        try {
            // 密码验证
            if (newPassword == null || newPassword.length() < 6) {
                return ArticleResponseData.fail("密码长度不能少于6位");
            }

            userService.updateUserPassword(userId, newPassword);
            logger.info("重置用户密码成功，用户ID: {}", userId);
            return ArticleResponseData.ok("密码重置成功");
        } catch (Exception e) {
            logger.error("重置用户密码失败，错误信息: {}", e.getMessage());
            return ArticleResponseData.fail("密码重置失败");
        }
    }

    // 删除用户
    @PostMapping("/delete")
    @ResponseBody
    public ArticleResponseData<String> deleteUser(@RequestParam Integer userId) {
        try {
            // 防止删除自己
            // 在实际应用中，可以从SecurityContext中获取当前登录用户ID进行比较

            userService.deleteUser(userId);
            logger.info("删除用户成功，用户ID: {}", userId);
            return ArticleResponseData.ok("用户删除成功");
        } catch (Exception e) {
            logger.error("删除用户失败，错误信息: {}", e.getMessage());
            return ArticleResponseData.fail("删除用户失败");
        }
    }

    // 获取用户详情
    @GetMapping("/detail/{userId}")
    @ResponseBody
    public ArticleResponseData<User> getUserDetail(@PathVariable Integer userId) {
        try {
            User user = userService.findUserById(userId);
            if (user == null) {
                return ArticleResponseData.fail("用户不存在");
            }
            return ArticleResponseData.ok(user);
        } catch (Exception e) {
            logger.error("获取用户详情失败，错误信息: {}", e.getMessage());
            return ArticleResponseData.fail("获取用户详情失败");
        }
    }

    // 更新用户信息
    @PostMapping("/update")
    @ResponseBody
    public ArticleResponseData<String> updateUser(@RequestParam Integer userId,
                                                  @RequestParam String username,
                                                  @RequestParam String email) {
        try {
            User user = userService.findUserById(userId);
            if (user == null) {
                return ArticleResponseData.fail("用户不存在");
            }

            // 检查用户名是否已被其他用户使用
            User existingUser = userService.findUserByUsername(username);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                return ArticleResponseData.fail("用户名已被使用");
            }

            // 检查邮箱是否已被其他用户使用
            existingUser = userService.findUserByEmail(email);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                return ArticleResponseData.fail("邮箱已被使用");
            }

            user.setUsername(username);
            user.setEmail(email);
            userService.updateUser(user);

            logger.info("更新用户信息成功，用户ID: {}", userId);
            return ArticleResponseData.ok("用户信息更新成功");
        } catch (Exception e) {
            logger.error("更新用户信息失败，错误信息: {}", e.getMessage());
            return ArticleResponseData.fail("更新用户信息失败");
        }
    }

    // 获取用户统计信息
    @GetMapping("/statistics")
    @ResponseBody
    public ArticleResponseData<UserStatistics> getUserStatistics() {
        try {
            UserStatistics statistics = userService.getUserStatistics();
            return ArticleResponseData.ok(statistics);
        } catch (Exception e) {
            logger.error("获取用户统计信息失败，错误信息: {}", e.getMessage());
            return ArticleResponseData.fail("获取统计信息失败");
        }
    }

    // 修改用户角色
    @PostMapping("/change-role")
    @ResponseBody
    public ArticleResponseData<String> changeUserRole(@RequestParam Integer userId,
                                                      @RequestParam String role) {
        try {
            // 角色验证，只允许改为admin或common
            if (!"admin".equals(role) && !"common".equals(role)) {
                return ArticleResponseData.fail("角色不存在");
            }

            // 首先获取用户当前的权限，然后更新
            User user = userService.findUserById(userId);
            if (user == null) {
                return ArticleResponseData.fail("用户不存在");
            }

            // 获取当前用户的权限
            List<String> authorities = user.getAuthorities();
            // 判断当前用户是否已经是目标角色
            boolean isAdmin = authorities.contains("ROLE_admin");
            if (("admin".equals(role) && isAdmin) || ("common".equals(role) && !isAdmin)) {
                return ArticleResponseData.fail("用户已经是该角色");
            }

            // 更新用户角色：先删除所有权限，再添加新权限
            userMapper.deleteUserAuthorities(userId);
            if ("admin".equals(role)) {
                userMapper.saveUserAuthority(userId, 1); // 1表示ROLE_admin
            } else {
                userMapper.saveUserAuthority(userId, 2); // 2表示ROLE_common
            }

            logger.info("修改用户角色成功，用户ID: {}, 新角色: {}", userId, role);
            return ArticleResponseData.ok("角色修改成功");
        } catch (Exception e) {
            logger.error("修改用户角色失败，错误信息: {}", e.getMessage());
            return ArticleResponseData.fail("角色修改失败");
        }
    }
}