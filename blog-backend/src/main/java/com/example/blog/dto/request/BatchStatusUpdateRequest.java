package com.example.blog.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BatchStatusUpdateRequest {
    private List<Long> userIds;
    private Integer status;
}