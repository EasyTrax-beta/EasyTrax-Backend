package com.easytrax.easytraxbackend.packinglist.domain.repository;

import com.easytrax.easytraxbackend.packinglist.domain.PackingList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PackingListRepository extends JpaRepository<PackingList, Long> {

    @Query("SELECT p FROM PackingList p WHERE p.project.id = :projectId AND p.project.user.id = :userId")
    Page<PackingList> findByProjectIdAndUserId(@Param("projectId") Long projectId, 
                                              @Param("userId") Long userId, 
                                              Pageable pageable);

    @Query("SELECT p FROM PackingList p LEFT JOIN FETCH p.items WHERE p.id = :id AND p.project.user.id = :userId")
    Optional<PackingList> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT p FROM PackingList p WHERE p.project.user.id = :userId")
    Page<PackingList> findByUserId(@Param("userId") Long userId, Pageable pageable);
}