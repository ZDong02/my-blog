package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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

    // 查询所有用户
    List<User> findAllUsers();

    // 更新用户角色
    int updateUserRole(@Param("userId") Long userId, @Param("role") String role);

    // 更新用户状态
    int updateUserStatus(@Param("userId") Long userId, @Param("status") Integer status);

    // 批量更新用户状态
    int batchUpdateStatus(@Param("userIds") List<Long> userIds, @Param("status") Integer status);

    // 批量删除用户
    int batchDeleteUsers(@Param("userIds") List<Long> userIds);

    // 统计今日新增用户数
    int countTodayNewUsers();

    // 统计本月新增用户数
    int countMonthNewUsers();

    // 统计活跃用户数（近30天有登录）
    int countActiveUsers();

    // 统计角色分布
    Map<String, Integer> countRoleDistribution();

    // 获取用户增长趋势（近30天每日新增）
    List<Map<String, Object>> getGrowthTrend(@Param("days") int days);

    // 按ID列表查询用户
    List<User> selectUsersByIds(@Param("userIds") List<Long> userIds);
}