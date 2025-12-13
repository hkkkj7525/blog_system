package com.example.blog_system.dao;

import com.example.blog_system.model.domain.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

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
}