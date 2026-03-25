package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.learning.api.entity.Tutor;
import java.util.List;

@Repository
public interface TutorRepo extends JpaRepository<Tutor, Long> {
    /** 依 status 查詢，並以申請日期排序 */
    List<Tutor> findByStatusOrderByApplyDateAsc(Integer status);

    @Query("""
        SELECT
            SUM(CASE WHEN t.status = 1 THEN 1 ELSE 0 END),
            SUM(CASE WHEN t.status = 2 THEN 1 ELSE 0 END),
            SUM(CASE WHEN t.status = 3 THEN 1 ELSE 0 END)
        FROM Tutor t
    """)
    Object[] countTutorStatus();
}
