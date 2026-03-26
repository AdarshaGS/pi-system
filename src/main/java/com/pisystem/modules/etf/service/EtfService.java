package com.investments.etf.service;

import com.investments.etf.data.Etf;
import com.investments.etf.repo.EtfRepository;
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
