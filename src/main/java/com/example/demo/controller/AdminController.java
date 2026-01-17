package com.example.demo.controller;

import com.example.demo.entity.Admin;
import com.example.demo.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*") // เผื่อเรียกจาก Flutter
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Admin admin) {

        Map<String, Object> response = new HashMap<>();

        // ===============================
        // 🔥 HARD CODE ADMIN (ไม่ต้องสมัคร)
        // ===============================
        if ("admin".equals(admin.getName()) &&
            "admin1234".equals(admin.getPassword())) {

            response.put("success", true);
            response.put("role", "ADMIN");
            response.put("adminName", "admin");

            return response;
        }

        // ===============================
        // 🔁 login ปกติ (กรณีมี admin ใน DB)
        // ===============================
        boolean success = adminService.login(
            admin.getName(),
            admin.getPassword()
        );

        if (success) {
            response.put("success", true);
            response.put("role", "ADMIN");
            response.put("adminName", admin.getName());
        } else {
            response.put("success", false);
            response.put("message", "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
        }

        return response;
    }
}
