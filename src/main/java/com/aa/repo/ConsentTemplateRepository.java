package com.aa.repo;

import com.aa.data.ConsentTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsentTemplateRepository extends JpaRepository<ConsentTemplateEntity, Long> {
}
