package com.learning.api.dto.Admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminTutorReviewReq {
    /**
     * 審核結果：
     * 2 = 准予開通 (qualified)
     * 3 = 退回/停權
     */
    private Integer status;
}
