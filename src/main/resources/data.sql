-- Dummy data for User entity
-- Note: Password hashes are dummy bcrypt-like hashes for testing purposes only

INSERT INTO users (username, email, password_hash, created_at, updated_at) VALUES
('john_doe', 'john.doe@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '2023-01-15 10:30:00', '2023-01-15 10:30:00'),
('jane_smith', 'jane.smith@email.com', '$2a$10$DOwJHjcCaLNQqRXdP0WFP.HuJvxQA5zJNOOWwgTSzQFkUWyVpwDl6', '2023-02-20 14:15:30', '2023-02-25 16:45:20'),
('bob_wilson', 'bob.wilson@email.com', '$2a$10$8K.9S9jVgCcG0qFUKtRkFe5lJuOKwmUkfCwGCqSNGhEZjSHQO5LOS', '2023-03-10 09:20:45', '2023-03-10 09:20:45'),
('alice_johnson', 'alice.johnson@email.com', '$2a$10$7hGPQXfTJw8bCDDVfKxO2OYdmW9uULPNVNXPOmk2eOWQCOzJ5RWiG', '2023-04-05 11:55:12', '2023-04-12 13:22:35'),
('charlie_brown', 'charlie.brown@email.com', '$2a$10$RZKCfQRUvQXDJFgvLJn3nOdXlMZhZmGJfNdKnMGYfGTfVGLsJZwPW', '2023-05-18 08:40:25', '2023-05-18 08:40:25'),
('diana_prince', 'diana.prince@email.com', '$2a$10$LmNpQfRHvJdWjFgKGdnT2OzUKJGJGYLPzGJKhMoWKqNMXOJfQdSJK', '2023-06-02 15:30:00', '2023-06-08 10:15:45'),
('test_user', 'test@example.com', '$2a$10$MnVpKfLGvJsWdFaKPqnF5OuUGJnLGYJPnGJMhVoTMnLMXKJfPsSKR', '2023-07-01 12:00:00', '2023-07-01 12:00:00'),
('admin_user', 'admin@peahdb.com', '$2a$10$PzXpLfJGvGdWjKaKJqnD6OyUHJoLGZJPoGJNhWoUNoLNXLJfQdTJL', '2023-08-15 09:45:30', '2023-08-20 14:30:15'); 