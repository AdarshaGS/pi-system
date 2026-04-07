package com.pisystem.modules.sms.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.springframework.stereotype.Service;

import com.pisystem.modules.sms.repo.SmsRegexPatternRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Central registry for SMS regex patterns.
 *
 * <p>Patterns are loaded from the {@code sms_regex_patterns} database table
 * at application startup. If no DB entry exists for a given key the caller
 * supplies a static fallback, so the parser always has sensible defaults.
 *
 * <h3>Usage in parsers</h3>
 * <pre>
 *   patternRegistry.get("PARSER_AMOUNT_1", AMOUNT_PATTERN).matcher(message)
 * </pre>
 *
 * <h3>Refreshing without restart</h3>
 * Call {@link #refresh()} from the admin endpoint
 * {@code POST /api/v1/sms/patterns/refresh} after updating a row in the DB.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsPatternRegistry {

    private final SmsRegexPatternRepository repository;

    /** Thread-safe cache: patternKey → compiled Pattern. */
    private final Map<String, Pattern> cache = new ConcurrentHashMap<>();

    // =========================================================================
    // Lifecycle
    // =========================================================================

    @PostConstruct
    public void load() {
        int loaded = 0;
        for (var entity : repository.findByIsActiveTrue()) {
            try {
                int flags = entity.isCaseInsensitive() ? Pattern.CASE_INSENSITIVE : 0;
                cache.put(entity.getPatternKey(), Pattern.compile(entity.getPatternValue(), flags));
                loaded++;
            } catch (PatternSyntaxException e) {
                // Bad regex in DB — log and skip so the parser falls back to static default
                log.error("Invalid regex for pattern key '{}' — skipping. Error: {}",
                        entity.getPatternKey(), e.getMessage());
            }
        }
        log.info("SmsPatternRegistry: loaded {} pattern(s) from database", loaded);
    }

    /**
     * Reload all patterns from DB without restarting the application.
     * Thread-safe: clears then repopulates the cache atomically per key.
     */
    public void refresh() {
        cache.clear();
        load();
        log.info("SmsPatternRegistry: patterns refreshed");
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Return the DB-overridden pattern for {@code key}, or {@code fallback}
     * if no active DB entry exists for that key.
     */
    public Pattern get(String key, Pattern fallback) {
        Pattern p = cache.get(key);
        return p != null ? p : fallback;
    }

    /**
     * Return the DB-overridden pattern for {@code key}, or {@code null} if
     * no active DB entry exists.
     */
    public Pattern get(String key) {
        return cache.get(key);
    }

    /** Number of patterns currently loaded in the cache. */
    public int size() {
        return cache.size();
    }
}
