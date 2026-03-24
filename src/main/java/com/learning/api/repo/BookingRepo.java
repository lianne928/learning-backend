package com.learning.api.repo;

import com.learning.api.dto.CheckoutReq;
import com.learning.api.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Long> {

    /**
     * 防超賣核心查詢：
     * 根據 老師ID、日期、小時，檢查是否已經存在「且仍在鎖定中(slotLocked=true)」的預約紀錄。
     */
    Optional<Booking> findByTutorIdAndDateAndHourAndSlotLockedTrue(Long tutorId, LocalDate date, Integer hour);

    Optional<Booking> findByStudentIdAndDateAndHourAndSlotLockedTrue(Long studentId, LocalDate date, Integer hour);

    List<Booking> findByTutorId(Long tutorId);

    /**
     * 查詢學生未來預約時段
     */
    @Query("""
        SELECT new com.learning.api.dto.CheckoutReq$Slot(b.date, b.hour)
        FROM Booking b
        WHERE b.studentId = :studentId
        AND b.slotLocked = true
        AND (
            (b.date > :startDate AND b.date < :endDate)
            OR (b.date = :startDate AND b.hour >= :startHour)
            OR (b.date = :endDate AND b.hour <= :endHour)
        )
        ORDER BY b.date, b.hour
        """)
    List<CheckoutReq.Slot> findStudentFutureBookings(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startHour") int startHour,
            @Param("endHour") int endHour
    );

    /**
     * 查詢老師未來預約時段
     */
    @Query("""
        SELECT new com.learning.api.dto.CheckoutReq$Slot(b.date, b.hour)
        FROM Booking b
        WHERE b.tutorId = :tutorId
        AND b.slotLocked = true
        AND (
            (b.date > :startDate AND b.date < :endDate)
            OR (b.date = :startDate AND b.hour >= :startHour)
            OR (b.date = :endDate AND b.hour <= :endHour)
        )
        ORDER BY b.date, b.hour
        """)
    List<CheckoutReq.Slot> findTutorFutureBookings(
            @Param("tutorId") Long tutorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startHour") int startHour,
            @Param("endHour") int endHour
    );

    /**
     * 找出已過期（時間已過）且 status=1 的 booking，用來撥款給老師
     */
    @Query("SELECT b FROM Booking b WHERE b.slotLocked = true AND b.status = 1 AND (b.date < :today OR (b.date = :today AND b.hour < :hour))")
    List<Booking> findExpiredBookings(@Param("today") LocalDate today, @Param("hour") int hour);

    /**
     * 批次更新過期 booking 為 status=2（已完成）
     */
    @Modifying
    @Query("UPDATE Booking b SET b.status = 2 WHERE b.slotLocked = true AND b.status = 1 AND (b.date < :today OR (b.date = :today AND b.hour < :hour))")
    void updateExpiredBookings(@Param("today") LocalDate today, @Param("hour") int hour);
}