package com.users.consent.service;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.users.consent.data.Consent;
import com.users.consent.data.ConsentRequest;
import com.users.consent.data.ConsentTemplate;
import com.users.consent.repo.ConsentRepository;

@Service
public class ConsentServiceImpl implements ConsentService {

    private final ConsentRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public ConsentServiceImpl(final ConsentRepository repository, final JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long createConsentForUser(ConsentRequest consentRequest) {
        Consent consent = Consent.builder().userId(consentRequest.getUserId()).consentId(consentRequest.getConsentId())
                .agreed(true).build();
        this.repository.save(consent);
        return consent.getId();
    }

    @Override
    public List<ConsentTemplate> getConsentTemplates() {
        final String sql = "select * from consent_template";
        return this.jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ConsentTemplate.class));
    }

}
