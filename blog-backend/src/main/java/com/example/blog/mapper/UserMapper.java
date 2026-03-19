package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 按用户名查询
    User findByUsername(@Param("username") String username);

    // 按邮箱查询
    User findByEmail(@Param("email") String email);

    // 更新个人资料
    int updateProfile(User user);

    // 标记为非首个用户
    int markAsNotFirstUser(@Param("userId") Long userId);

    // 统计用户总数
    int countTotalUsers();

    // 查询用户及统计信息
    User selectWithStats(Long id);

    // 查询所有头像为空的用户
    java.util.List<User> findAllUsersWithNullAvatar();
}