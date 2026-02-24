package com.example.demo.controller;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ResultsDTO;
import com.example.demo.entity.Results;
import com.example.demo.service.ResultsService;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultsService service;

    public ResultController(ResultsService service) {
        this.service = service;
    }

    // =========================
    // ✅ สร้างผลตรวจ
    // =========================
    @PostMapping("/create")
    public ResponseEntity<ResultsDTO> createResult(@RequestBody Results result) {
        Results saved = service.saveResult(result);
        ResultsDTO dto = ResultsDTO.fromEntity(saved);
        return ResponseEntity
                .created(URI.create("/api/results/" + saved.getId()))
                .body(dto);
    }

    // =========================
    // ✅ ดึงผลตรวจตาม shopId (DTO เท่านั้น)
    // =========================
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ResultsDTO>> getResultsByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(service.getResultsByShopDto(shopId));
    }

    // =========================
    // ✅ ดึงผลตรวจตามชื่อผัก (DTO เท่านั้น)
    // =========================
    @GetMapping("/vege/{vegeName}")
    public List<ResultsDTO> getResultsByVege(@PathVariable String vegeName) {
        return service.getResultsByVegeNameDto(vegeName);
    }

    // =========================
    // ✅ ดึงผลตรวจทั้งหมด (DTO เท่านั้น)
    // =========================
    @GetMapping("/all")
    public List<ResultsDTO> getAllResults() {
        return service.getAllResultsDTO();
    }

    // =========================
    // ✅ shopname + location (map)
    // =========================
    @GetMapping("/shops-location")
    public List<ResultsDTO> getShopnameAndLocation() {
        return service.getShopnameAndLocation();
    }

    // =========================
    // ✅ หมุดที่ APPROVED เท่านั้น
    // =========================
    @GetMapping("/map-pins")
    public List<ResultsDTO> getApprovedShopsForMapAll() {
        return service.getApprovedShopsForMap(null);
    }

    // =========================
    // 📄 Certificate Upload
    // =========================
    @PostMapping(
            path = "/{id}/certificate-upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadCertificate(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Empty file");
        }
        service.setCertificate(id, file.getBytes());
        return ResponseEntity.ok("Uploaded certificate for result " + id);
    }

    // =========================
    // ✅ ดาวน์โหลดใบรับรอง (resu_id เดียว)
    // =========================
    @GetMapping("/{resuId}/certificate")
    public ResponseEntity<byte[]> getCertificate(@PathVariable Long resuId) {
        byte[] data = service.getCertificate(resuId);
        if (data == null || data.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("certificate_" + resuId + ".pdf")
                        .build()
        );
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    // =========================
    // (ออปชัน) เส้นคู่ shopId
    // =========================
    @PostMapping(
            path = "/shop/{shopId}/result/{resuId}/certificate-upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadCertificateByShop(
            @PathVariable Long shopId,
            @PathVariable Long resuId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Empty file");
        }
        service.setCertificateFor(resuId, shopId, file.getBytes());
        return ResponseEntity.ok(
                "Uploaded certificate for result " + resuId + " (shop " + shopId + ")"
        );
    }

    @GetMapping("/shop/{shopId}/result/{resuId}/certificate")
    public ResponseEntity<byte[]> getCertificateByShop(
            @PathVariable Long shopId,
            @PathVariable Long resuId
    ) {
        byte[] data = service.getCertificateFor(resuId, shopId);
        if (data == null || data.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("certificate_" + resuId + "_shop_" + shopId + ".pdf")
                        .build()
        );
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
