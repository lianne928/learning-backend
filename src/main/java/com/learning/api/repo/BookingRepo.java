package com.learning.api.repo;

import com.learning.api.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Long> {

    List<Booking> findByTutorId(Long tutorId);

    List<Booking> findByStudentId(Long studentId);

    List<Booking> findByOrderIdIn(List<Long> orderIds);

    Optional<Booking> findByTutorIdAndDateAndHour(Long tutorId, LocalDate date, Integer hour);

    Optional<Booking> findByStudentIdAndDateAndHourAndSlotLockedTrue(Long studentId, LocalDate date, Integer hour);
    Optional<Booking> findByTutorIdAndDateAndHourAndSlotLockedTrue(Long tutorId, LocalDate date, Integer hour);

    List<Booking> findByOrderId(Long orderId);

    List<Booking> findByStudentIdAndDateOrderByHourAsc(Long studentId, LocalDate date);
    List<Booking> findByStudentIdAndDateGreaterThanEqualOrderByDateAscHourAsc(Long studentId, LocalDate date);
    Optional<Booking> findByIdAndStudentId(Long id, Long studentId);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.studentId = :studentId
        AND b.slotLocked = true
        AND (
            (b.date > :startDate AND b.date < :endDate)
            OR (b.date = :startDate AND b.hour >= :startHour)
            OR (b.date = :endDate AND b.hour <= :endHour)
        )
        ORDER BY b.date, b.hour
        """)
    List<Booking> findStudentFutureBookings(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("startHour") int startHour,
        @Param("endHour") int endHour
    );

    @Query("""
        SELECT b FROM Booking b
        WHERE b.tutorId = :tutorId
        AND b.slotLocked = true
        AND (
            (b.date > :startDate AND b.date < :endDate)
            OR (b.date = :startDate AND b.hour >= :startHour)
            OR (b.date = :endDate AND b.hour <= :endHour)
        )
        ORDER BY b.date, b.hour
        """)
    List<Booking> findTutorFutureBookings(
        @Param("tutorId") Long tutorId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("startHour") int startHour,
        @Param("endHour") int endHour
    );

    @Query("""
        SELECT b FROM Booking b
        WHERE b.status = 1
        AND (b.date < :today OR (b.date = :today AND b.hour < :hour))
        """)
    List<Booking> findExpiredBookings(@Param("today") LocalDate today, @Param("hour") int hour);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Booking b SET b.status = 2
        WHERE b.status = 1
        AND (b.date < :today OR (b.date = :today AND b.hour < :hour))
        """)
    void updateExpiredBookings(@Param("today") LocalDate today, @Param("hour") int hour);
}
