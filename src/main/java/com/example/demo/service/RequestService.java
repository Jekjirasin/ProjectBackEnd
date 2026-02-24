package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.AllRequestsGroupedDTO;
import com.example.demo.dto.GroupedRequestDTO;
import com.example.demo.dto.RequestsDTO;
import com.example.demo.dto.ShopsDTO;
import com.example.demo.entity.Request;
import com.example.demo.entity.Shops;
import com.example.demo.repository.RequestRepository;
import com.example.demo.repository.ResultsRepository;
import com.example.demo.repository.ShopRepository;

@Service
public class RequestService {

    private final RequestRepository repository;
    private final ResultsRepository resultsRepository;
    private final ShopRepository shopRepository;

    public RequestService(RequestRepository repository,
                          ResultsRepository resultsRepository,
                          ShopRepository shopRepository) {
        this.repository = repository;
        this.resultsRepository = resultsRepository;
        this.shopRepository = shopRepository;
    }

    // ==========================
    // Basic queries
    // ==========================

    public List<Request> getRequestsByShop(Long shopId) {
        return repository.findByShopId(shopId);
    }

    public List<Request> getRequestsByStatus(String status) {
        return repository.findByStatus(status);
    }

    public List<Request> getAllRequests() {
        return repository.findAll();
    }

    // ==========================
    // Create
    // ==========================

    @Transactional
    public RequestsDTO createRequest(Request req) {

        if (req.getShop() == null || req.getShop().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "shop.id is required");
        }

        Shops shop = shopRepository.findById(req.getShop().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Shop not found with id: " + req.getShop().getId()
                ));

        req.setShop(shop);

        if (req.getShopLocation() == null || req.getShopLocation().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "shopLocation is required (format: 'lat,lng')"
            );
        }

        double[] ll = parseLatLng(req.getShopLocation());
        double lat = ll[0];
        double lng = ll[1];

        if (isLocationUsedByAnother(shop.getId(), lat, lng)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "ตำแหน่งนี้ถูกใช้ในคำขอของร้านอื่นแล้ว"
            );
        }

        if (req.getStatus() == null || req.getStatus().isBlank()) {
            req.setStatus("pending");
        }

        Request saved = repository.save(req);
        return mapToDTO(saved);
    }

    // ==========================
    // Update
    // ==========================

    @Transactional
    public Request updateStatus(Long requestId, String status) {
        Request request = repository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Request not found with id: " + requestId
                ));
        request.setStatus(status);
        return repository.save(request);
    }

    @Transactional
    public Request updateAppointmentDay(Long requestId, LocalDate appointmentDay) {
        Request request = repository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Request not found with id: " + requestId
                ));
        request.setAppointmentDay(appointmentDay);
        return repository.save(request);
    }
    @Transactional
public void updateAppointmentDayForGroup(Long shopId,
                                         LocalDate dateInspection,
                                         LocalDate appointmentDay) {

    List<Request> requests =
            repository.findByShopIdAndDateInspection(shopId, dateInspection);

    for (Request req : requests) {
        req.setAppointmentDay(appointmentDay);
    }

    repository.saveAll(requests);
}

@Transactional
public void updateStatusByAppointmentDayAndShop(Long shopId,
                                                LocalDate appointmentDay,
                                                String status) {

    List<Request> requests =
            repository.findByShopIdAndAppointmentDay(shopId, appointmentDay);

    for (Request req : requests) {
        req.setStatus(status);
    }

    repository.saveAll(requests);
}
    // ==========================
    // Grouped endpoints (FIX 500)
    // ==========================

    public List<AllRequestsGroupedDTO> getAllGroupedByDate() {

        List<Request> requests = repository.findAll();

        Map<LocalDate, List<RequestsDTO>> grouped = requests.stream()
                .filter(r -> r.getDateInspection() != null) // 🔥 กัน null
                .map(this::mapToDTO)
                .filter(dto -> dto != null && dto.getDateInspection() != null)
                .collect(Collectors.groupingBy(RequestsDTO::getDateInspection));

        return grouped.entrySet().stream()
                .map(e -> new AllRequestsGroupedDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public List<GroupedRequestDTO> getRequestsGroupedByDate(Long shopId) {

        List<Request> requests = repository.findByShopId(shopId);

        Map<LocalDate, List<RequestsDTO>> grouped = requests.stream()
                .filter(r -> r.getDateInspection() != null)
                .map(this::mapToDTO)
                .filter(dto -> dto != null && dto.getDateInspection() != null)
                .collect(Collectors.groupingBy(RequestsDTO::getDateInspection));

        return grouped.entrySet().stream()
                .map(entry -> new GroupedRequestDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    // ==========================
    // Helpers
    // ==========================

    private double[] parseLatLng(String shopLocation) {
        String[] parts = shopLocation.split(",");
        if (parts.length != 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "shopLocation format must be 'lat,lng'"
            );
        }
        try {
            double lat = Double.parseDouble(parts[0].trim());
            double lng = Double.parseDouble(parts[1].trim());
            return new double[]{lat, lng};
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid latitude/longitude"
            );
        }
    }

    public boolean isLocationUsedByAnother(Long shopId, double lat, double lng) {
        Integer dup = repository.existsSameLocationInRequests(shopId, lat, lng);
        return dup != null && dup == 1;
    }

    private RequestsDTO mapToDTO(Request r) {

        if (r.getShop() == null) {
            return null; // 🔥 กัน null shop
        }

        ShopsDTO shopDTO = new ShopsDTO(
                r.getShop().getId(),
                r.getShop().getShopName(),
                r.getShop().getOwnerFname(),
                r.getShop().getOwnerLname(),
                r.getShop().getHouseNumber(),
                r.getShop().getMoo(),
                r.getShop().getStreet(),
                r.getShop().getTumbon(),
                r.getShop().getAmper(),
                r.getShop().getProvince(),
                r.getShop().getPhone()
        );

        RequestsDTO dto = new RequestsDTO(
                r.getId(),
                shopDTO,
                r.getVegeName(),
                r.getShopLocation(),
                r.getDateInspection(),
                r.getAppointmentDay(),
                r.getStatus()
        );

        resultsRepository.findByRequest_Id(r.getId())
                .ifPresent(res -> dto.setResuId(res.getId()));

        return dto;
    }
}