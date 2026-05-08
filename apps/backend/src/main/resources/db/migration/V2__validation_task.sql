CREATE TABLE validation_task (
    id                 BIGSERIAL PRIMARY KEY,
    status             VARCHAR(20) NOT NULL,
    attempts           INTEGER NOT NULL,
    last_attempt_at    TIMESTAMP WITH TIME ZONE,
    failure_reason     VARCHAR(1000),
    validation_pair_id UUID NOT NULL,
    edge_id            BIGINT NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_validation_task_validation_pair_id UNIQUE (validation_pair_id)
);