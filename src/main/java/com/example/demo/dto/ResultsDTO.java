package com.example.demo.dto;

import com.example.demo.entity.Results;

public class ResultsDTO {
    private Long resuId;
    private Long shopId;
    private String shopName;
    private String vegeName;
    private String result;
    private String location;
    private String dateInspection;   // ✅ ตรงกับ entity
    private String certificateUrl;

    public ResultsDTO() {}

    public ResultsDTO(Long resuId, Long shopId, String shopName, String vegeName,
                      String result, String location, String dateInspection, String certificateUrl) {
        this.resuId = resuId;
        this.shopId = shopId;
        this.shopName = shopName;
        this.vegeName = vegeName;
        this.result = result;
        this.location = location;
        this.dateInspection = dateInspection;
        this.certificateUrl = certificateUrl;
    }

    public static ResultsDTO fromEntity(Results r) {
        if (r == null) return null;
        return new ResultsDTO(
                r.getId(),
                (r.getShop() != null ? r.getShop().getId() : null),
                r.getShopName(),
                r.getVegeName(),
                r.getResult(),
                r.getLocation(),
                r.getDateInspection(),   // ✅ ใช้ชื่อจริง
                null
        );
    }
    public ResultsDTO(Long resuId, Long shopId, String shopName, String vegeName,
                  String result, String location) {
    this(resuId, shopId, shopName, vegeName, result, location, null, null);
    }
    // -------- getters & setters --------
    public Long getResuId() { return resuId; }
    public void setResuId(Long resuId) { this.resuId = resuId; }

    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getVegeName() { return vegeName; }
    public void setVegeName(String vegeName) { this.vegeName = vegeName; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDateInspection() { return dateInspection; }
    public void setDateInspection(String dateInspection) { this.dateInspection = dateInspection; }

    public String getCertificateUrl() { return certificateUrl; }
    public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }
}
