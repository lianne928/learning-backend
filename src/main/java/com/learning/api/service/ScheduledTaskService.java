package com.learning.api.service;

import com.learning.api.entity.Booking;
import com.learning.api.entity.WalletLog;
import com.learning.api.repo.BookingRepo;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.WalletLogsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ScheduledTaskService {

    @Autowired private BookingRepo     bookingRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private WalletLogsRepo  walletLogRepo;

    /**
     * 每小時執行一次：
     * 1. 找出所有已過期（時間已過）且 status=1 的 booking
     * 2. 對每筆完成的 booking 撥款給老師（新增 wallet_log）
     * 3. 批次更新 status=2（已完成）
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void updateCompletedBookings() {
        LocalDate today       = LocalDate.now();
        int       currentHour = LocalTime.now().getHour();

        // 1. 找出即將變成已完成的 booking（status=1 且時間已過）
        List<Booking> toComplete = bookingRepo.findExpiredBookings(today, currentHour);

        // 2. 對每筆完成的 booking 撥款給老師
        toComplete.forEach(b -> {
            orderRepo.findById(b.getOrderId()).ifPresent(order -> {

                // 防止重複撥款：檢查是否已有此 booking 的撥款記錄
                String tradeNo = "TUTOR_EARN_" + b.getId();
                boolean alreadyPaid = walletLogRepo
                        .findByUserIdOrderByCreatedAtDesc(b.getTutorId())
                        .stream()
                        .anyMatch(log -> tradeNo.equals(log.getMerchantTradeNo()));

                if (alreadyPaid) {
                    System.out.println("⚠️ 已撥款，略過 bookingId=" + b.getId());
                    return;
                }

                // 建立老師收入 wallet_log
                WalletLog log = new WalletLog();
                log.setUserId(b.getTutorId());           // 老師的 userId
                log.setTransactionType(3);               // 3 = 授課收入
                log.setAmount((long) order.getDiscountPrice()); // 折扣後單堂價
                log.setRelatedType(2);                   // 2 = 關聯到 booking
                log.setRelatedId(b.getId());             // 綁定 booking ID
                log.setMerchantTradeNo(tradeNo);         // 唯一交易序號
                walletLogRepo.save(log);

                System.out.println("✅ 已撥款給老師 tutorId=" + b.getTutorId()
                        + "，金額=" + order.getDiscountPrice()
                        + "，bookingId=" + b.getId());
            });
        });

        // 3. 批次更新 status=2（已完成）
        if (!toComplete.isEmpty()) {
            bookingRepo.updateExpiredBookings(today, currentHour);
            System.out.println("✅ 已更新 " + toComplete.size() + " 筆預約為已完成");
        }
    }
}