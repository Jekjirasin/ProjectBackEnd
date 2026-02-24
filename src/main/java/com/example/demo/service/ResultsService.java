package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ResultsDTO;
import com.example.demo.entity.Request;
import com.example.demo.entity.Results;
import com.example.demo.entity.Shops;
import com.example.demo.repository.RequestRepository;
import com.example.demo.repository.ResultsRepository;
import com.example.demo.repository.ShopRepository;

@Service
public class ResultsService {

    private final ResultsRepository repository;
    private final ShopRepository shopRepository;
    private final RequestRepository requestRepository;

    @Value("${server.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    public ResultsService(
            ResultsRepository repository,
            ShopRepository shopRepository,
            RequestRepository requestRepository
    ) {
        this.repository = repository;
        this.shopRepository = shopRepository;
        this.requestRepository = requestRepository;
    }

    // =========================================================
    // 🟢 CREATE
    // =========================================================

    @Transactional
    public Results saveResult(Results result) {

        if (result.getShop() == null || result.getShop().getId() == null) {
            throw new IllegalArgumentException("shop.id is required");
        }

        Shops managedShop = shopRepository.findById(result.getShop().getId())
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

        result.setShop(managedShop);

        if (result.getRequest() != null && result.getRequest().getId() != null) {
            Request req = requestRepository.findById(result.getRequest().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Request not found"));
            result.setRequest(req);
        }

        if (result.getResult() != null) {
            result.setResult(result.getResult().toUpperCase());
        }

        return repository.save(result);
    }

    // =========================================================
    // 🟢 READ (DTO SAFE – ไม่โหลด LOB ตรงๆ)
    // =========================================================

    @Transactional(readOnly = true)
    public List<ResultsDTO> getResultsByShopDto(Long shopId) {

        return repository.findByShop_Id(shopId).stream().map(r -> {

            String certUrl = null;
            if (r.getCertificate() != null && r.getCertificate().length > 0) {
                certUrl = publicBaseUrl + "/api/results/" + r.getId() + "/certificate";
            }

            return new ResultsDTO(
                    r.getId(),
                    r.getShop() != null ? r.getShop().getId() : null,
                    r.getShopName(),
                    r.getVegeName(),
                    r.getResult(),
                    r.getLocation(),
                    r.getDateInspection(),
                    certUrl
            );
        }).toList();
    }

    // 🔥 ตัวนี้ที่ error บอกว่าไม่มี
    @Transactional(readOnly = true)
    public List<ResultsDTO> getAllResultsDTO() {
        return repository.findAll().stream()
                .map(ResultsDTO::fromEntity)
                .toList();
    }

    // 🔥 ตัวนี้ที่ error บอกว่าไม่มี
    @Transactional(readOnly = true)
    public List<ResultsDTO> getResultsByVegeNameDto(String vegeName) {
        return repository.findByVegeName(vegeName).stream()
                .map(ResultsDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResultsDTO> getApprovedShopsForMap(Long shopId) {
        return repository.findApprovedShopsForMap("APPROVED", shopId);
    }

    @Transactional(readOnly = true)
    public List<ResultsDTO> getShopnameAndLocation() {
        return repository.findShopnameAndLocation();
    }

    // =========================================================
    // 📄 CERTIFICATE (LOB SAFE)
    // =========================================================

    @Transactional
    public Results setCertificate(Long resultId, byte[] pdfBytes) {
        Results r = repository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("Result not found"));

        r.setCertificate(pdfBytes);
        return repository.save(r);
    }

    @Transactional(readOnly = true)
    public byte[] getCertificate(Long resultId) {
        return repository.findById(resultId)
                .map(Results::getCertificate)
                .orElse(null);
    }

    // ✅ ป้องกัน LOB error
    @Transactional(readOnly = true)
    public byte[] getCertificateByRequest(Long requestId) {

        Optional<Long> resultIdOpt = repository.findResultIdByRequestId(requestId);

        if (resultIdOpt.isEmpty()) {
            return null;
        }

        Long resultId = resultIdOpt.get();

        return repository.findById(resultId)
                .map(Results::getCertificate)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Optional<Results> getByIdAndShop(Long resuId, Long shopId) {
        return repository.findByIdAndShop_Id(resuId, shopId);
    }

    @Transactional
    public Results setCertificateFor(Long resuId, Long shopId, byte[] pdfBytes) {
        Results r = repository.findByIdAndShop_Id(resuId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Result not found"));

        r.setCertificate(pdfBytes);
        return repository.save(r);
    }

    @Transactional(readOnly = true)
    public byte[] getCertificateFor(Long resuId, Long shopId) {
        return repository.findByIdAndShop_Id(resuId, shopId)
                .map(Results::getCertificate)
                .orElse(null);
    }
}