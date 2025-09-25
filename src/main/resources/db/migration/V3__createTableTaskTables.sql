CREATE TABLE task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    statement TEXT NOT NULL,
    task_order INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_task_course_statement UNIQUE (course_id, statement),
    CONSTRAINT fk_task_course FOREIGN KEY (course_id) REFERENCES Course(id) ON DELETE CASCADE
);

CREATE TABLE task_option (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    option TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_task_option_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
);