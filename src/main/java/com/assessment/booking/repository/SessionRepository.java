package com.assessment.booking.repository;

import com.assessment.booking.entity.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SessionRepository extends JpaRepository<ClassSession, Long> {

    @Query("""
            select s
            from ClassSession s
            where s.offering.id = :offeringId
            order by s.startTimeUtc
            """)
    List<ClassSession> findByOfferingIdOrderByStartTimeUtc(@Param("offeringId") Long offeringId);

    @Query("""
            select s
            from ClassSession s
            where s.offering.id in :offeringIds
            order by s.startTimeUtc
            """)
    List<ClassSession> findByOfferingIdsOrderByStartTimeUtc(@Param("offeringIds") Collection<Long> offeringIds);

    @Query("""
            select s
            from ClassSession s
            where s.offering.id in (
                select b.offering.id
                from Booking b
                where b.parent.id = :parentId
            )
            order by s.startTimeUtc
            """)
    List<ClassSession> findBookedSessionsByParentId(@Param("parentId") Long parentId);
}

