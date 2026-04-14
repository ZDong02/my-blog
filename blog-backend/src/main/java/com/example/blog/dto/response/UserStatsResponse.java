package com.example.blog.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class UserStatsResponse {
    private Integer totalUsers;
    private Integer todayNewUsers;
    private Integer monthNewUsers;
    private Integer activeUsers;
    private Map<String, Integer> roleDistribution;
    private List<GrowthData> growthTrend;

    @Data
    public static class GrowthData {
        private String date;
        private Integer count;
    }
}
