package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.dto.ResultsDTO;
import com.example.demo.entity.Results;

public interface ResultsRepository extends JpaRepository<Results, Long> {

    @Query("""
        SELECT new com.example.demo.dto.ResultsDTO(
            r.id,
            r.shop.id,
            r.shopName,
            r.vegeName,
            r.result,
            r.location,
            r.dateInspection,
            null
        )
        FROM Results r
        WHERE r.shop.id = :shopId
    """)
    List<ResultsDTO> findDTOByShop(@Param("shopId") Long shopId);

    @Query("""
        SELECT new com.example.demo.dto.ResultsDTO(
            r.id,
            r.shop.id,
            r.shopName,
            r.vegeName,
            r.result,
            r.location,
            r.dateInspection,
            null
        )
        FROM Results r
    """)
    List<ResultsDTO> findAllDTO();

    Optional<Results> findByIdAndShop_Id(Long id, Long shopId);
    Optional<Results> findByRequest_Id(Long requestId);
}