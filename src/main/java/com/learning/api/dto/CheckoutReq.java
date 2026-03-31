package com.learning.api.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CheckoutReq {
    private Long studentId;
    private Long courseId;
    private Integer lessonCount;
    private List<Slot> selectedSlots; // 學生選的多個時段
    private Boolean isExperienced;    // 是否為體驗課（前端傳 true 時啟用）

    @Getter
    @Setter
    public static class Slot {
        private LocalDate date;
        private Integer hour;

        public Slot(LocalDate date, Integer hour) {
            this.date = date;
            this.hour = hour;
        }
    }

}