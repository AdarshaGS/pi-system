package com.pisystem.modules.sms.data;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single named regex pattern stored in the database.
 *
 * <p>Storing patterns here lets an admin update or add new bank-specific
 * regex without a code change or redeploy. {@link com.pisystem.modules.sms.service.SmsPatternRegistry}
 * loads all active patterns at startup and exposes them to the parser.
 *
 * <h3>Pattern key naming convention</h3>
 * <ul>
 *   <li>{@code PARSER_*}     — used by {@code SMSParserService}</li>
 *   <li>{@code CLASSIFIER_*} — used by {@code SmsClassifierServiceImpl}</li>
 * </ul>
 *
 * <h3>patternValue format</h3>
 * <p>Store the raw regex string exactly as it would appear inside
 * {@code Pattern.compile("…")}. Use single backslashes where the regex
 * engine needs a backslash (e.g. {@code \b}, {@code \s}, {@code \.}).
 * The registry calls {@code Pattern.compile(patternValue, …)} directly.
 */
@Entity
@Table(name = "sms_regex_patterns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRegexPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique logical name, e.g. {@code PARSER_AMOUNT_1}. */
    @Column(name = "pattern_key", nullable = false, unique = true, length = 100)
    private String patternKey;

    /** The raw regex string passed to {@code Pattern.compile()}. */
    @Column(name = "pattern_value", nullable = false, columnDefinition = "TEXT")
    private String patternValue;

    /**
     * When true, the pattern is compiled with {@code Pattern.CASE_INSENSITIVE}.
     * Defaults to true for all bank SMS patterns.
     */
    @Column(name = "case_insensitive", nullable = false)
    @Builder.Default
    private boolean caseInsensitive = true;

    @Column(length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
