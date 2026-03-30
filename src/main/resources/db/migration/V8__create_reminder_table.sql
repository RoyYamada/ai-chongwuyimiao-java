CREATE TABLE reminder (
    id BIGSERIAL PRIMARY KEY,
    template_id VARCHAR(255) NOT NULL,
    vaccination_id BIGINT NOT NULL REFERENCES vaccination(id),
    reminder_date DATE NOT NULL,
    reminder_thing TEXT NOT NULL,
    location TEXT,
    target_name TEXT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_reminder_vaccination_id ON reminder(vaccination_id);