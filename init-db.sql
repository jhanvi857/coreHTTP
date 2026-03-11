-- Create tasks table
CREATE TABLE IF NOT EXISTS tasks (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Initial data
INSERT INTO tasks (title, description, completed) VALUES 
('Launch NioFlow', 'Finalize the NIO core and documentation.', true),
('Implement CRUD', 'Add SQL persistence and task controller.', false),
('Add Auth', 'Integrate JWT and middleware security.', false);
