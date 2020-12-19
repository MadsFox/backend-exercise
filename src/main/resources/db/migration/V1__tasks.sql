CREATE TABLE tasks
(
    id              VARCHAR(36) NOT NULL PRIMARY KEY,
    description     TEXT NOT NULL,
    status          TEXT NOT NULL DEFAULT 'pending',
    createdAt       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO tasks (id, description) VALUES
    ('4e404522-1c56-11eb-adc1-0242ac120002', 'demo task 1', 'pending'),
    ('5420698d-791d-4edd-9dea-a3dc4947b68a', 'demo task 2', 'completed'),
    ('6e9370bf-fa01-4e63-941a-0ecfc451aff7', 'demo task 3', 'pending');