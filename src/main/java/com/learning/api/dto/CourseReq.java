package com.learning.api.dto;

import lombok.Data;

@Data
public class CourseReq {
    private String name;
    private Integer subject;
    private String description;
    private Integer price;
    private Boolean isActive;
    private Integer level;
}
