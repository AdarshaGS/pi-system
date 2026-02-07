-- V51: Create Credit Scores Table
-- Migration to support credit score tracking and monitoring
-- Enables multi-provider credit score history and trend analysis

CREATE TABLE IF NOT EXISTS credit_scores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    score INTEGER NOT NULL CHECK (score >= 300 AND score <= 850),
    provider VARCHAR(50) NOT NULL,
    record_date TIMESTAMP NOT NULL,
    score_rating VARCHAR(20) NOT NULL CHECK (score_rating IN (
        'POOR', 'FAIR', 'GOOD', 'VERY_GOOD', 'EXCELLENT'
    )),
    change_from_previous INTEGER,
    factors VARCHAR(1000),
    recommendations VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_credit_score_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for performance optimization
CREATE INDEX idx_credit_scores_user_id ON credit_scores(user_id);
CREATE INDEX idx_credit_scores_provider ON credit_scores(provider);
CREATE INDEX idx_credit_scores_record_date ON credit_scores(record_date);
CREATE INDEX idx_credit_scores_user_provider ON credit_scores(user_id, provider);
CREATE INDEX idx_credit_scores_user_date ON credit_scores(user_id, record_date DESC);
CREATE INDEX idx_credit_scores_rating ON credit_scores(score_rating);

-- Note: Score rating should be calculated in application code
-- MySQL triggers and views have different syntax than PostgreSQL
    provider,
    record_date,
    score_rating,
    change_from_previous,
    factors,
    recommendations
FROM credit_scores
ORDER BY user_id, provider, record_date DESC;

COMMENT ON VIEW latest_credit_scores IS 'Shows the most recent credit score for each user from each provider';
