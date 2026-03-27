package com.pisystem.modules.etf.controller;

import com.pisystem.shared.features.FeatureFlag;
import com.pisystem.shared.features.RequiresFeature;
import com.pisystem.modules.etf.data.Etf;
import com.pisystem.modules.etf.service.EtfService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/etf")
@RequiresFeature(FeatureFlag.INVESTMENTS_MODULE)
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
    public Etf getEtfBySymbol(@PathVariable("symbol") String symbol) {
        return etfService.getEtfBySymbol(symbol);
    }
}
