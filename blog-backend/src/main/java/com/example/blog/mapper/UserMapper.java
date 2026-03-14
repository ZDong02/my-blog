package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE username = #{username} AND status = 1")
    User findByUsername(@Param("username") String username);

    @Select("SELECT * FROM users WHERE email = #{email} AND status = 1")
    User findByEmail(@Param("email") String email);

    @Update("UPDATE users SET nickname = #{nickname}, avatar = #{avatar}, bio = #{bio}, updated_at = NOW() WHERE id = #{id}")
    int updateProfile(User user);

    @Update("UPDATE users SET is_first_user = 0 WHERE id = #{userId}")
    int markAsNotFirstUser(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM users")
    int countTotalUsers();
}