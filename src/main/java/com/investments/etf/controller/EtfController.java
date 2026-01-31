package com.investments.etf.controller;

import com.investments.etf.data.Etf;
import com.investments.etf.service.EtfService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/etf")
@RequiredArgsConstructor
@Tag(name = "ETF Module", description = "APIs for managing ETFs")
public class EtfController {

    private final EtfService etfService;

    @GetMapping
    public List<Etf> getAllEtfs() {
        return etfService.getAllEtfs();
    }

    @PostMapping
    public Etf addEtf(@Valid @RequestBody Etf etf) {
        return etfService.addEtf(etf);
    }

    @GetMapping("/{symbol}")
    public Etf getEtfBySymbol(@PathVariable String symbol) {
        return etfService.getEtfBySymbol(symbol);
    }
}
