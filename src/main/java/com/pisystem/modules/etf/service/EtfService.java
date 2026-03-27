package com.pisystem.modules.etf.service;

import com.pisystem.modules.etf.data.Etf;
import com.pisystem.modules.etf.repo.EtfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EtfService {

    private final EtfRepository etfRepository;

    public List<Etf> getAllEtfs() {
        return etfRepository.findAll();
    }

    public Etf addEtf(Etf etf) {
        return etfRepository.save(etf);
    }

    public Etf getEtfBySymbol(String symbol) {
        return etfRepository.findBySymbol(symbol).orElse(null);
    }
}
