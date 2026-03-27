package com.pisystem.modules.etf.repo;

import com.pisystem.modules.etf.data.Etf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EtfRepository extends JpaRepository<Etf, Long> {
    Optional<Etf> findBySymbol(String symbol);
}
