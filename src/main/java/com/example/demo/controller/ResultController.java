package com.example.demo.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/create")
    public ResponseEntity<ResultsDTO> create(@RequestBody Results result) {
        Results saved = service.saveResult(result);
        return ResponseEntity
                .created(URI.create("/api/results/" + saved.getId()))
                .build();
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ResultsDTO>> getByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(service.getResultsByShop(shopId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ResultsDTO>> getAll() {
        return ResponseEntity.ok(service.getAllResults());
    }
}