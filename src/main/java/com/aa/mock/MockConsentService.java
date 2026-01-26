package com.aa.mock;

import com.aa.data.*;
import com.aa.repo.AAConsentRepository;
import com.aa.repo.ConsentTemplateRepository;
import com.common.data.EntityType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MockConsentService {

    private final AAConsentRepository consentRepository;
    private final ConsentTemplateRepository templateRepository;

    public ConsentResponse createConsent(ConsentRequest request) {
        String consentId = "mock-consent-" + UUID.randomUUID().toString().substring(0, 8);
        String consentTemplateId = request.getConsentTemplateId();
        if (consentTemplateId == null || consentTemplateId.isEmpty()) {
            consentTemplateId = consentId;
        }

        List<FIType> fiTypes = request.getFiTypes() != null ? request.getFiTypes() : Collections.emptyList();
        String fiTypesStr = fiTypes.stream().map(Enum::name).collect(Collectors.joining(","));

        String validFrom = request.getValidFrom() != null ? request.getValidFrom() : LocalDateTime.now().toString();
        String validTill = request.getValidTill() != null ? request.getValidTill()
                : LocalDateTime.now().plusMonths(6).toString();

        AAConsentEntity entity = AAConsentEntity.builder()
                .consentId(consentId)
                .consentTemplateId(consentTemplateId)
                .userId(request.getUserId())
                .status(ConsentStatus.ACTIVE)
                .fiTypes(fiTypesStr)
                .validFrom(validFrom)
                .validTill(validTill)
                .entityType(request.getEntityType())
                .build();

        consentRepository.save(entity);

        return ConsentResponse.builder()
                .consentId(consentId)
                .userConsentId(consentTemplateId)
                .status(entity.getStatus())
                .fiTypes(fiTypes)
                .validFrom(validFrom)
                .validTill(validTill)
                .redirectUrl("http://localhost:8082/mock-aa/redirect?consentId=" + consentId)
                .entityType(entity.getEntityType())
                .build();
    }

    public ConsentStatus getConsentStatus(String consentId) {
        return consentRepository.findByConsentId(consentId)
                .or(() -> consentRepository.findByConsentTemplateId(consentId))
                .map(entity -> {
                    // Auto-expiry check
                    if (LocalDateTime.parse(entity.getValidTill()).isBefore(LocalDateTime.now())
                            && entity.getStatus() == ConsentStatus.ACTIVE) {
                        entity.setStatus(ConsentStatus.EXPIRED);
                        consentRepository.save(entity);
                    }
                    return entity.getStatus();
                })
                .orElse(null);
    }

    public void revokeConsent(String consentId) {
        consentRepository.findByConsentId(consentId)
                .or(() -> consentRepository.findByConsentTemplateId(consentId))
                .ifPresent(entity -> {
                    entity.setStatus(ConsentStatus.REVOKED);
                    consentRepository.save(entity);
                });
    }

    public boolean isConsentValid(String consentId, FIType requiredType) {
        return consentRepository.findByConsentId(consentId)
                .or(() -> consentRepository.findByConsentTemplateId(consentId))
                .map(entity -> {
                    if (entity.getStatus() != ConsentStatus.ACTIVE) {
                        return false;
                    }
                    // Check expiry
                    if (LocalDateTime.parse(entity.getValidTill()).isBefore(LocalDateTime.now())) {
                        entity.setStatus(ConsentStatus.EXPIRED);
                        consentRepository.save(entity);
                        return false;
                    }
                    List<String> types = Arrays.asList(entity.getFiTypes().split(","));
                    return types.contains(requiredType.name());
                })
                .orElse(false);
    }

    public List<FIType> getFiTypesForConsent(String consentId) {
        return consentRepository.findByConsentId(consentId)
                .or(() -> consentRepository.findByConsentTemplateId(consentId))
                .map(entity -> {
                    if (entity.getFiTypes() == null || entity.getFiTypes().isEmpty()) {
                        return Collections.<FIType>emptyList();
                    }
                    return Arrays.stream(entity.getFiTypes().split(","))
                            .map(FIType::valueOf)
                            .collect(Collectors.toList());
                })
                .orElse(Collections.emptyList());
    }

    public List<Map<String, String>> getConsentTemplates() {
        List<ConsentTemplateEntity> templates = templateRepository.findAll();
        String entityType = null;
        if (templates.isEmpty()) {
            return List.of(
                    Map.of("templateId", "ONE_TIME_KYC", "description",
                            "One-time access for account verification and identity validation", "entityType", "7"),
                    Map.of("templateId", "CREDIT_ASSESSMENT", "description",
                            "Comprehensive data access for credit scoring and loan underwriting", "entityType", "10"),
                    Map.of("templateId", "PFM_RECURRING", "description",
                            "Periodic access for personal finance management and expense analysis", "entityType", "6"),
                    Map.of("templateId", "WEALTH_ADVISORY", "description",
                            "Ongoing access for wealth management, portfolio tracking, and investment advice",
                            "entityType", "2"),
                    Map.of("templateId", "TAX_COMPLIANCE", "description",
                            "Specific access for automated tax filing and financial audits", "entityType", "24"));
        }
        List<Map<String, String>> consentTemplateMaps = templates.stream()
                .map(t -> Map.of(
                        "templateId", String.valueOf(t.getId()),
                        "description", t.getMessage(),
                        "entityType", EntityType.fromId(t.getEntityType()).getName()))
                .collect(Collectors.toList());

        return consentTemplateMaps;
    }

}
