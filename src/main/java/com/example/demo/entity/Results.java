package com.example.demo.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "results")
public class Results {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resu_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shops shop;

    @Column(name = "resu_shopname", nullable = false)
    private String shopName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "req_id", referencedColumnName = "req_id")
    private Request request;

    @Column(name = "resu_vegename", nullable = false)
    private String vegeName;

    @Column(name = "resu_result")
    private String result;

    // ✅ FIX: บังคับโหลด LOB ทันที กัน Unable to access lob stream
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "resu_certificate")
    private byte[] certificate;

    @Column(name = "resu_location")
    private String location;

    @Column(name = "resu_dateinspection")
    private String dateInspection;

    // ---------------------------
    // Getter & Setter
    // ---------------------------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Shops getShop() { return shop; }
    public void setShop(Shops shop) { this.shop = shop; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public Request getRequest() { return request; }
    public void setRequest(Request request) { this.request = request; }

    public String getVegeName() { return vegeName; }
    public void setVegeName(String vegeName) { this.vegeName = vegeName; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public byte[] getCertificate() { return certificate; }
    public void setCertificate(byte[] certificate) { this.certificate = certificate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDateInspection() { return dateInspection; }
    public void setDateInspection(String dateInspection) { this.dateInspection = dateInspection; }
}
