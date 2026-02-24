package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.dto.ResultsDTO;
import com.example.demo.entity.Results;

public interface ResultsRepository extends JpaRepository<Results, Long> {

    // โหลด shop มาด้วย (กัน LazyInitializationException)
    @EntityGraph(attributePaths = {"shop"})
    List<Results> findByShop_Id(Long shopId);

    List<Results> findByVegeName(String vegeName);

    Optional<Results> findByRequest_Id(Long requestId);

    Optional<Results> findByIdAndShop_Id(Long id, Long shopId);

    // ใช้กับ ResultsService.getShopnameAndLocation()
    @Query("""
        SELECT new com.example.demo.dto.ResultsDTO(
            r.id,
            r.shop.id,
            r.shopName,
            r.vegeName,
            r.result,
            r.location
        )
        FROM Results r
    """)
    List<ResultsDTO> findShopnameAndLocation();

    // ใช้กับ ResultsService.getApprovedShopsForMap(...)
    @Query("""
        SELECT DISTINCT new com.example.demo.dto.ResultsDTO(
            r.id,
            r.shop.id,
            r.shopName,
            r.vegeName,
            r.result,
            r.location
        )
        FROM Results r
        WHERE UPPER(r.result) = :approved
          AND r.location IS NOT NULL
          AND r.location <> ''
          AND (:shopId IS NULL OR r.shop.id = :shopId)
    """)
    List<ResultsDTO> findApprovedShopsForMap(
            @Param("approved") String approved,
            @Param("shopId") Long shopId
    );
}
