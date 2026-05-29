package com.assessment.booking.repository;

import com.assessment.booking.entity.Offering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OfferingRepository extends JpaRepository<Offering, Long> {

    @Query("""
            select o
            from Offering o
            join fetch o.course
            join fetch o.teacher
            where o.id = :id
            """)
    Optional<Offering> findDetailedById(@Param("id") Long id);

    @Query("""
            select o
            from Offering o
            join fetch o.course
            join fetch o.teacher
            order by o.createdAt desc
            """)
    List<Offering> findAllDetailed();

    @Query("""
            select o
            from Offering o
            join fetch o.course
            join fetch o.teacher
            where o.teacher.id = :teacherId
            order by o.createdAt desc
            """)
    List<Offering> findDetailedByTeacherId(@Param("teacherId") Long teacherId);
}

