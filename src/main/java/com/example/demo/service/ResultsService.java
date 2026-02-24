package com.example.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.ResultsDTO;
import com.example.demo.entity.Results;
import com.example.demo.entity.Shops;
import com.example.demo.repository.ResultsRepository;
import com.example.demo.repository.ShopRepository;

@Service
public class ResultsService {

    private final ResultsRepository repository;
    private final ShopRepository shopRepository;

    public ResultsService(ResultsRepository repository,
                          ShopRepository shopRepository) {
        this.repository = repository;
        this.shopRepository = shopRepository;
    }

    @Transactional
    public Results saveResult(Results result) {

        if (result.getShop() == null || result.getShop().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "shopId required");
        }

        Shops shop = shopRepository.findById(result.getShop().getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found"));

        result.setShop(shop);

        if (result.getResult() != null) {
            result.setResult(result.getResult().toUpperCase());
        }

        return repository.save(result);
    }

    @Transactional(readOnly = true)
    public List<ResultsDTO> getResultsByShop(Long shopId) {
        return repository.findDTOByShop(shopId);
    }

    @Transactional(readOnly = true)
    public List<ResultsDTO> getAllResults() {
        return repository.findAllDTO();
    }
}