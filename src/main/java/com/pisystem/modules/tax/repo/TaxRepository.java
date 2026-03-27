package com.pisystem.modules.tax.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.pisystem.modules.tax.data.Tax;

public interface TaxRepository extends JpaRepository<Tax, Long>, JpaSpecificationExecutor<Tax>{
    
}
