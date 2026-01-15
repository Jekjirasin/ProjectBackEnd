package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "req_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    private Shops shop;

    @Column(name = "req_vegename")
    private String vegeName;

    @Column(name = "req_shoplocation")
    private String shopLocation;

    @Column(name = "req_dateinspection")
    private LocalDate dateInspection;

    @Column(name = "req_appointmentday")
    private LocalDate appointmentDay;

    // ✅ FIX: ใช้ String + VARCHAR (ไม่ใช้ ENUM)
    @Column(name = "req_status", nullable = false)
    private String status = "pending";

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Shops getShop() { return shop; }
    public void setShop(Shops shop) { this.shop = shop; }

    public String getShopLocation() { return shopLocation; }
    public void setShopLocation(String shopLocation) { this.shopLocation = shopLocation; }

    public String getVegeName() { return vegeName; }
    public void setVegeName(String vegeName) { this.vegeName = vegeName; }

    public LocalDate getDateInspection() { return dateInspection; }
    public void setDateInspection(LocalDate dateInspection) { this.dateInspection = dateInspection; }

    public LocalDate getAppointmentDay() { return appointmentDay; }
    public void setAppointmentDay(LocalDate appointmentDay) { this.appointmentDay = appointmentDay; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
