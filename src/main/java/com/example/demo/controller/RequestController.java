package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;          // ✅ เพิ่ม import นี้

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AllRequestsGroupedDTO;
import com.example.demo.dto.GroupedRequestDTO;
import com.example.demo.dto.RequestsDTO;
import com.example.demo.dto.ShopsDTO;
import com.example.demo.entity.Request;
import com.example.demo.service.RequestService;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final RequestService service;

    public RequestController(RequestService service) {
        this.service = service;
    }

    // ✅ ใหม่: เช็คพิกัดซ้ำในตาราง requests
    // GET /api/requests/location-check?shopId=1&lat=19.685753&lng=99.745410
    @GetMapping("/location-check")
    public ResponseEntity<?> checkDuplicateLocation(
            @RequestParam Long shopId,
            @RequestParam double lat,
            @RequestParam double lng) {
        boolean conflict = service.isLocationUsedByAnother(shopId, lat, lng);
        if (conflict) {
            return ResponseEntity.status(409)
                    .body(Map.of("ok", false, "message", "ตำแหน่งนี้ถูกใช้ในคำขอของร้านอื่นแล้ว"));
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // 📌 สร้างคำขอใหม่
    @PostMapping("/create")
    public RequestsDTO createRequest(@RequestBody Request request) {
        return service.createRequest(request);
    }

    // 📌 ดึงคำขอทั้งหมด (group)
    @GetMapping("/all")
    public List<AllRequestsGroupedDTO> getAllRequestsGrouped() {
        return service.getAllGroupedByDate();
    }

    // 📌 ดึงคำขอตาม shopId (group)
    @GetMapping("/shop/{shopId}")
    public List<GroupedRequestDTO> getRequestsGrouped(@PathVariable Long shopId) {
        return service.getRequestsGroupedByDate(shopId);
    }
    // ✅ เพิ่มตัวนี้สำหรับหน้า shop
@GetMapping("/grouped-by-shop/{shopId}")
public List<GroupedRequestDTO> getGroupedByShop(@PathVariable Long shopId) {
    return service.getRequestsGroupedByDate(shopId);
}
    // 📌 ดึงคำขอตามสถานะ
    @GetMapping("/status/{status}")
    public List<Request> getRequestsByStatus(@PathVariable String status) {
        return service.getRequestsByStatus(status);
    }

    // ✅ อัปเดตสถานะรายรายการ
    @PutMapping("/{id}/status")
    public RequestsDTO updateStatus(@PathVariable Long id,
                                    @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        Request updated = service.updateStatus(id, status.toLowerCase());  
        return toDto(updated);
    }

    // 📌 อัปเดตสถานะทั้งกลุ่ม
    @PutMapping("/status-by-appointment")
    public String updateStatusByAppointment(@RequestBody Map<String, String> body) {
        Long shopId = Long.parseLong(body.get("shopId"));
        LocalDate appointmentDay = LocalDate.parse(body.get("appointmentDay"));
        String status = body.get("status");
        service.updateStatusByAppointmentDayAndShop(shopId, appointmentDay, status);
        return "Status updated for shopId: " + shopId + " with appointmentDay: " + appointmentDay;
    }

    // 📌 อัปเดตวันนัด (ทั้งกลุ่ม)
    @PutMapping("/appointment-group")
    public String updateAppointmentDayForGroup(@RequestBody Map<String, String> body) {
        Long shopId = Long.parseLong(body.get("shopId"));
        LocalDate dateInspection = LocalDate.parse(body.get("dateInspection"));
        LocalDate appointmentDay = LocalDate.parse(body.get("appointmentDay"));
        service.updateAppointmentDayForGroup(shopId, dateInspection, appointmentDay);
        return "Appointment updated for all requests in this group";
    }

    // 📌 อัปเดตวันนัด (รายรายการ)
    @PutMapping("/{id}/appointment")
    public RequestsDTO updateAppointmentDay(@PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        LocalDate appointmentDay = LocalDate.parse(body.get("appointmentDay"));
        return toDto(service.updateAppointmentDay(id, appointmentDay));
    }

    @GetMapping("/grouped-by-date/{shopId}/{dateInspection}")
    public GroupedRequestDTO getGroupedByDateByShopAndDate(@PathVariable Long shopId,
                                                            @PathVariable String dateInspection) {
        LocalDate date = LocalDate.parse(dateInspection);
        return service.getRequestsGroupedByDateInspectionByShopAndDate(shopId, date);
    }

    // -------- helper แปลง Entity → DTO --------
    private RequestsDTO toDto(Request saved) {
        ShopsDTO shopDTO = new ShopsDTO(
                saved.getShop().getId(),
                saved.getShop().getShopName(),
                saved.getShop().getOwnerFname(),
                saved.getShop().getOwnerLname(),
                saved.getShop().getHouseNumber(),
                saved.getShop().getMoo(),
                saved.getShop().getStreet(),
                saved.getShop().getTumbon(),
                saved.getShop().getAmper(),
                saved.getShop().getProvince(),
                saved.getShop().getPhone()
        );
        return new RequestsDTO(
                saved.getId(),
                shopDTO,
                saved.getVegeName(),
                saved.getShopLocation(),
                saved.getDateInspection(),
                saved.getAppointmentDay(),
                saved.getStatus()
        );
    }
}
