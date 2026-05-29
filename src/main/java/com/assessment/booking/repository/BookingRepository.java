package com.assessment.booking.repository;

import com.assessment.booking.entity.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select b
            from Booking b
            where b.parent.id = :parentId
            order by b.id
            """)
    List<Booking> findByParentIdForUpdate(@Param("parentId") Long parentId);

    @Query("""
            select b
            from Booking b
            join fetch b.offering o
            join fetch o.course
            join fetch o.teacher
            where b.parent.id = :parentId
            order by b.bookedAt desc
            """)
    List<Booking> findDetailedByParentId(@Param("parentId") Long parentId);

    long countByParentId(Long parentId);
}

