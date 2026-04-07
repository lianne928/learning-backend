package com.learning.api.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TutorReviewCountDTO {
    private Long pendingCount;
    private Long qualifiedCount;
    private Long bannedCount;
}