CREATE TABLE users (
                       id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                       name          VARCHAR(255) NOT NULL,
                       email         VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role          VARCHAR(50)  NOT NULL DEFAULT 'USER',
                       created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE resumes (
                         id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                         user_id      UUID         REFERENCES users(id) ON DELETE SET NULL,
                         file_name    VARCHAR(255) NOT NULL,
                         storage_path VARCHAR(500) NOT NULL,
                         parsed_text  TEXT,
                         uploaded_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE resume_versions (
                                 id             UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
                                 resume_id      UUID             NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                                 version_number INT              NOT NULL,
                                 parsed_text    TEXT,
                                 overall_score  DOUBLE PRECISION,
                                 created_at     TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE analysis_results (
                                  id                        UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
                                  resume_id                 UUID             NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                                  ats_score                 DOUBLE PRECISION,
                                  readability_score         DOUBLE PRECISION,
                                  section_completeness      JSONB,
                                  action_verb_analysis      JSONB,
                                  quantifiable_achievements JSONB,
                                  grammar_issues            JSONB,
                                  created_at                TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE job_descriptions (
                                  id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                  user_id            UUID         REFERENCES users(id) ON DELETE SET NULL,
                                  title              VARCHAR(255),
                                  raw_text           TEXT         NOT NULL,
                                  extracted_keywords JSONB,
                                  created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role_fit_analyses (
                                   id                 UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
                                   resume_id          UUID             NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                                   job_description_id UUID             NOT NULL REFERENCES job_descriptions(id) ON DELETE CASCADE,
                                   fit_score          DOUBLE PRECISION,
                                   explanation        TEXT,
                                   matched_keywords   JSONB,
                                   missing_keywords   JSONB,
                                   created_at         TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cover_letters (
                               id                 UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
                               resume_id          UUID      NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                               job_description_id UUID      NOT NULL REFERENCES job_descriptions(id) ON DELETE CASCADE,
                               content            TEXT      NOT NULL,
                               created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE interview_questions (
                                     id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                     resume_id  UUID         NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                                     category   VARCHAR(100),
                                     question   TEXT         NOT NULL,
                                     created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE salary_estimates (
                                  id               UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
                                  resume_id        UUID             NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
                                  role_title       VARCHAR(255),
                                  experience_years INT,
                                  estimated_range  VARCHAR(100),
                                  currency         VARCHAR(10),
                                  created_at       TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE benchmarks (
                            id              UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
                            industry        VARCHAR(100)     NOT NULL,
                            role            VARCHAR(100)     NOT NULL,
                            avg_score       DOUBLE PRECISION NOT NULL,
                            percentile_data JSONB            NOT NULL
);

CREATE TABLE refresh_tokens (
                                id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                token      VARCHAR(255) NOT NULL UNIQUE,
                                expires_at TIMESTAMP    NOT NULL,
                                used       BOOLEAN      NOT NULL DEFAULT FALSE,
                                created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_resumes_user_id               ON resumes(user_id);
CREATE INDEX idx_resume_versions_resume_id     ON resume_versions(resume_id);
CREATE INDEX idx_analysis_results_resume_id    ON analysis_results(resume_id);
CREATE INDEX idx_job_descriptions_user_id      ON job_descriptions(user_id);
CREATE INDEX idx_role_fit_analyses_resume_id   ON role_fit_analyses(resume_id);
CREATE INDEX idx_role_fit_analyses_jd_id       ON role_fit_analyses(job_description_id);
CREATE INDEX idx_interview_questions_resume_id ON interview_questions(resume_id);
CREATE INDEX idx_salary_estimates_resume_id    ON salary_estimates(resume_id);
CREATE INDEX idx_refresh_tokens_user_id        ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token          ON refresh_tokens(token);
CREATE INDEX idx_benchmarks_industry_role      ON benchmarks(industry, role);