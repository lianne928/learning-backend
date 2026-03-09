package com.learning.api.dto;

import io.swagger.v3.oas.models.links.Link;
import lombok.Data;

@Data
public class TutorsRequest {
    private String intro;
    private Link videoUrl1,videoUrl2;

}
