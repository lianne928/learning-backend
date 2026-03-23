package com.learning.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String name;
    private Integer subject;
    private String description;
    private Integer price;
    private Boolean isActive;
}
