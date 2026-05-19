CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    total DECIMAL(10,2),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0
);

ALTER TABLE users ADD COLUMN phone VARCHAR(20);
ALTER TABLE users ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE orders ADD COLUMN shipping_address TEXT;

ALTER TABLE orders ADD CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE UNIQUE INDEX idx_users_email ON users (email);
CREATE INDEX idx_products_name ON products (name);

INSERT INTO users (id, name, email) VALUES
    (1, 'Alice Johnson', 'alice@example.com'),
    (2, 'Bob Smith', 'bob@example.com'),
    (3, 'Carol White', 'carol@example.com');

INSERT INTO products (id, name, price, stock) VALUES
    (1, 'Laptop', 999.99, 50),
    (2, 'Keyboard', 49.99, 200),
    (3, 'Mouse', 29.99, 150);

INSERT INTO orders (id, user_id, total, status) VALUES
    (1, 1, 1049.98, 'COMPLETED'),
    (2, 1, 29.99, 'PENDING'),
    (3, 2, 999.99, 'COMPLETED'),
    (4, 3, 79.98, 'SHIPPED');