package com.example.blog.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BatchDeleteRequest {
    private List<Long> userIds;
}