SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE task_option;
TRUNCATE TABLE task;
TRUNCATE TABLE course;
TRUNCATE TABLE users;
SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO users (id, createdat, name, email, role, password) VALUES
(1, CURRENT_TIMESTAMP, 'Caio', 'caio@alura.com.br', 'STUDENT', 'password123'),
(2, CURRENT_TIMESTAMP, 'Paulo', 'paulo@alura.com.br', 'INSTRUCTOR', 'password123');

INSERT INTO course (id, createdat, title, description, instructor_id, status) VALUES
(1, CURRENT_TIMESTAMP, 'Java', 'Aprenda Java com Alura', 2, 'BUILDING');
