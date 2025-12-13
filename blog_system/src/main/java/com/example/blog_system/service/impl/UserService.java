package com.example.blog_system.service.impl;

import com.example.blog_system.dao.UserMapper;
import com.example.blog_system.dao.UserStatistics;
import com.example.blog_system.model.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }


    public User findUserByUsername(String username) {
        User user = userMapper.findUserByUsername(username);
        if (user != null) {
            user.setAuthorities(userMapper.findAuthoritiesByUserId(user.getId()));
        }
        return user;
    }

    public User findUserByEmail(String email) {
        User user = userMapper.findUserByEmail(email);
        if (user != null) {
            user.setAuthorities(userMapper.findAuthoritiesByUserId(user.getId()));
        }
        return user;
    }

    public void saveUser(User user) {
        userMapper.saveUser(user);
        userMapper.saveUserAuthority(user.getId(), 2); // ROLE_common
    }

    public void updateUserStatus(Integer userId, boolean enabled) {
        userMapper.updateUserStatus(userId, enabled ? 1 : 2);
    }

    public void updateUserPassword(Integer userId, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updateUserPassword(userId, encodedPassword);
    }

    public List<User> findAllUsers() {
        List<User> users = userMapper.findAllUsers();
        for (User user : users) {
            user.setAuthorities(userMapper.findAuthoritiesByUserId(user.getId()));
        }
        return users;
    }

    public User findUserById(Integer userId) {
        User user = userMapper.findUserById(userId);
        if (user != null) {
            user.setAuthorities(userMapper.findAuthoritiesByUserId(user.getId()));
        }
        return user;
    }

    @Mapper
    public interface UserMapper {

        @Select("SELECT * FROM t_user WHERE username = #{username}")
        User findUserByUsername(String username);

        @Select("SELECT * FROM t_user WHERE email = #{email}")
        User findUserByEmail(String email);

        @Select("SELECT * FROM t_user WHERE id = #{userId}")
        User findUserById(Integer userId);

        @Select("SELECT * FROM t_user ORDER BY created DESC")
        List<User> findAllUsers();

        @Insert("INSERT INTO t_user(username, password, email, created, valid, image) " +
                "VALUES(#{username}, #{password}, #{email}, #{created}, #{valid}, #{image})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        void saveUser(User user);

        @Insert("INSERT INTO t_user_authority(user_id, authority_id) VALUES(#{userId}, #{authorityId})")
        void saveUserAuthority(@Param("userId") Integer userId, @Param("authorityId") Integer authorityId);

        @Update("UPDATE t_user SET valid = #{status} WHERE id = #{userId}")
        void updateUserStatus(@Param("userId") Integer userId, @Param("status") Integer status);

        @Update("UPDATE t_user SET password = #{password} WHERE id = #{userId}")
        void updateUserPassword(@Param("userId") Integer userId, @Param("password") String password);

        @Delete("DELETE FROM t_user WHERE id = #{userId}")
        void deleteUser(Integer userId);

        @Select("SELECT a.authority FROM t_authority a " +
                "JOIN t_user_authority ua ON a.id = ua.authority_id " +
                "WHERE ua.user_id = #{userId}")
        List<String> findAuthoritiesByUserId(Integer userId);

        @Delete("DELETE FROM t_user_authority WHERE user_id = #{userId}")
        void deleteUserAuthorities(Integer userId);

        @Update("UPDATE t_user SET username=#{username}, email=#{email}, image=#{image} WHERE id=#{id}")
        void updateUser(User user);

        // 搜索用户方法
        @Select("SELECT * FROM t_user WHERE username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%') ORDER BY created DESC")
        List<User> searchUsers(String keyword);

        // 获取用户统计信息方法
        @Select("SELECT " +
                "COUNT(*) as totalUsers, " +
                "COUNT(CASE WHEN valid = 1 THEN 1 END) as activeUsers, " +
                "COUNT(CASE WHEN valid = 2 THEN 1 END) as disabledUsers " +
                "FROM t_user")
        UserStatistics getUserStatistics();
    }
    @Transactional
    public void deleteUser(Integer userId) {
        // 先删除用户权限关联
        userMapper.deleteUserAuthorities(userId);
        // 再删除用户
        userMapper.deleteUser(userId);
    }

    // 更新用户信息（包括头像）
    public void updateUser(User user) {
        userMapper.updateUser(user);
    }

    // 搜索用户
    public List<User> searchUsers(String keyword) {
        List<User> users = userMapper.searchUsers(keyword);
        for (User user : users) {
            user.setAuthorities(userMapper.findAuthoritiesByUserId(user.getId()));
        }
        return users;
    }

    // 获取用户统计信息
    public UserStatistics getUserStatistics() {
        return userMapper.getUserStatistics();
    }
    // 修改用户角色
    @Transactional
    public void changeUserRole(Integer userId, String role) {
        // 删除现有权限
        userMapper.deleteUserAuthorities(userId);

        // 添加新权限
        Integer authorityId = "admin".equals(role) ? 1 : 2;
        userMapper.saveUserAuthority(userId, authorityId);
    }

    // 批量操作用户
    @Transactional
    public void batchUpdateUserStatus(List<Integer> userIds, boolean enabled) {
        for (Integer userId : userIds) {
            userMapper.updateUserStatus(userId, enabled ? 1 : 2);
        }
    }

    // 批量删除用户
    @Transactional
    public void batchDeleteUsers(List<Integer> userIds) {
        for (Integer userId : userIds) {
            userMapper.deleteUserAuthorities(userId);
            userMapper.deleteUser(userId);
        }
    }

    // 检查用户名是否存在
    public boolean isUsernameExists(String username) {
        return userMapper.findUserByUsername(username) != null;
    }

    // 检查邮箱是否存在
    public boolean isEmailExists(String email) {
        return userMapper.findUserByEmail(email) != null;
    }

    // 密码强度验证
    public boolean isPasswordValid(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        // 检查是否包含字母和数字
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        return hasLetter && hasDigit;
    }
}