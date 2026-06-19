ALTER TABLE analysis_results
    ADD COLUMN overall_score       DOUBLE PRECISION,
    ADD COLUMN keyword_gap_analysis JSONB,
    ADD COLUMN readability_data    JSONB;