package com.example.demo.service;

import com.example.demo.dto.ResultsDTO;
import com.example.demo.entity.Request;
import com.example.demo.entity.Results;
import com.example.demo.entity.Shops;
import com.example.demo.repository.RequestRepository;
import com.example.demo.repository.ResultsRepository;
import com.example.demo.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ResultsService {

    private final ResultsRepository repository;
    private final ShopRepository shopRepository;
    private final RequestRepository requestRepository;

    @Value("${server.public-base-url:http://10.32.110.29:8081}")
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
    // 🟢 CREATE / SAVE
    // =========================================================

    @Transactional
    public Results saveResult(Results result) {
        if (result.getShop() == null || result.getShop().getId() == null) {
            throw new IllegalArgumentException("shop.id is required");
        }

        Shops managedShop = shopRepository.findById(result.getShop().getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Shop not found: " + result.getShop().getId()));

        result.setShop(managedShop);

        if (result.getRequest() != null && result.getRequest().getId() != null) {
            Request req = requestRepository.findById(result.getRequest().getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Request not found: " + result.getRequest().getId()));
            result.setRequest(req);
        }

        // ✅ บังคับสถานะเป็นตัวพิมพ์ใหญ่ (APPROVED / REJECTED / PENDING)
        if (result.getResult() != null) {
            result.setResult(result.getResult().toUpperCase());
        }

        return repository.save(result);
    }

    @Transactional
    public Results saveResultWithShopId(Results result, Long shopId) {
        if (shopId == null) {
            throw new IllegalArgumentException("shopId is required");
        }

        Shops managedShop = shopRepository.findById(shopId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Shop not found: " + shopId));

        result.setShop(managedShop);

        if (result.getRequest() != null && result.getRequest().getId() != null) {
            Request req = requestRepository.findById(result.getRequest().getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Request not found: " + result.getRequest().getId()));
            result.setRequest(req);
        }

        if (result.getResult() != null) {
            result.setResult(result.getResult().toUpperCase());
        }

        return repository.save(result);
    }

    // =========================================================
    // 🟢 READ (DTO ONLY – กัน LOB พัง)
    // =========================================================

    /**
     * ใช้กับหน้า /api/results/shop/{shopId}
     */
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

    /**
     * ใช้กับ /api/results/all
     */
    @Transactional(readOnly = true)
    public List<ResultsDTO> getAllResultsDTO() {
        return repository.findAll().stream()
                .map(ResultsDTO::fromEntity)
                .toList();
    }

    /**
     * ใช้กับ /api/results/vege/{vegeName}
     */
    @Transactional(readOnly = true)
    public List<ResultsDTO> getResultsByVegeNameDto(String vegeName) {
        return repository.findByVegeName(vegeName).stream()
                .map(ResultsDTO::fromEntity)
                .toList();
    }

    /**
     * ใช้กับหน้าแผนที่ (เฉพาะ APPROVED)
     */
    @Transactional(readOnly = true)
    public List<ResultsDTO> getApprovedShopsForMap(Long shopId) {
        return repository.findApprovedShopsForMap("APPROVED", shopId);
    }

    /**
     * shopname + location (สำหรับ map แบบไม่กรองสถานะ)
     */
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
                .orElseThrow(() ->
                        new IllegalArgumentException("Result not found: " + resultId));

        r.setCertificate(pdfBytes);
        return repository.save(r);
    }

    @Transactional(readOnly = true)
    public byte[] getCertificate(Long resultId) {
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
                .orElseThrow(() ->
                        new IllegalArgumentException("Result not found for this shop"));

        r.setCertificate(pdfBytes);
        return repository.save(r);
    }

    @Transactional(readOnly = true)
    public byte[] getCertificateFor(Long resuId, Long shopId) {
        return repository.findByIdAndShop_Id(resuId, shopId)
                .map(Results::getCertificate)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public byte[] getCertificateByRequest(Long requestId) {
        return repository.findByRequest_Id(requestId)
                .map(Results::getCertificate)
                .orElse(null);
    }
}
