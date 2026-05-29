package com.assessment.booking.repository;

import com.assessment.booking.entity.Parent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Parent p where p.id = :id")
    Optional<Parent> findByIdForUpdate(@Param("id") Long id);
}

