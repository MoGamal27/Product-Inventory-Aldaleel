-- =============================================================
-- Aldaleel Raqamee — Product Inventory & Stock Reservation
-- Database Schema Backup
-- PostgreSQL 16
-- Generated: 2026-08-30
-- =============================================================

-- Create database (run this separately if needed)
-- CREATE DATABASE aldaleel_inventory;

-- Connect to the database before running the rest:
-- \c aldaleel_inventory

-- =============================================================
-- Drop tables if they exist (safe re-run)
-- =============================================================
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS products CASCADE;

-- =============================================================
-- Table: products
-- US-001 / TASK-001: Product entity with stock quantity
-- =============================================================
CREATE TABLE products (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255)   NOT NULL UNIQUE,
    description      TEXT,
    price            NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    stock_quantity   INTEGER        NOT NULL CHECK (stock_quantity >= 0),
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    version          BIGINT         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  products                IS 'Product catalogue with stock tracking';
COMMENT ON COLUMN products.stock_quantity IS 'Available units. Never goes below 0 (TASK-009)';
COMMENT ON COLUMN products.version        IS 'Optimistic lock version field (TASK-010)';

-- =============================================================
-- Table: orders
-- US-002 / TASK-005: Customer orders with state machine
-- =============================================================
CREATE TABLE orders (
    id          BIGSERIAL    PRIMARY KEY,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'DELIVERED')),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  orders        IS 'Customer orders. Status drives the state machine (TASK-015)';
COMMENT ON COLUMN orders.status IS 'PENDING → CONFIRMED → DELIVERED (terminal) | PENDING/CONFIRMED → CANCELLED (terminal)';

-- =============================================================
-- Table: order_items
-- US-002 / TASK-006: Line items linking orders to products
-- =============================================================
CREATE TABLE order_items (
    id          BIGSERIAL      PRIMARY KEY,
    order_id    BIGINT         NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id  BIGINT         NOT NULL REFERENCES products(id),
    quantity    INTEGER        NOT NULL CHECK (quantity >= 1),
    unit_price  NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0)
);

COMMENT ON TABLE  order_items            IS 'Order line items — each row reserves stock from a product';
COMMENT ON COLUMN order_items.unit_price IS 'Price snapshot at order time — immune to later product price changes';
COMMENT ON COLUMN order_items.quantity   IS 'Reserved quantity. Restored to products.stock_quantity on cancellation (TASK-013)';

-- =============================================================
-- Indexes for common query patterns
-- =============================================================
CREATE INDEX idx_products_name        ON products(name);
CREATE INDEX idx_orders_status        ON orders(status);
CREATE INDEX idx_order_items_order    ON order_items(order_id);
CREATE INDEX idx_order_items_product  ON order_items(product_id);

-- =============================================================
-- Sample data — for testing and demonstration
-- =============================================================

INSERT INTO products (name, description, price, stock_quantity) VALUES
    ('Dell XPS 15 Laptop',    'High-performance laptop with Intel Core i7, 16GB RAM', 1200.00, 50),
    ('Samsung 4K Monitor',    '27-inch 4K UHD IPS display with USB-C',                 450.00, 30),
    ('Mechanical Keyboard',   'Compact TKL with Cherry MX Red switches',                120.00, 100),
    ('Logitech MX Master 3',  'Advanced wireless mouse for productivity',                99.00, 75),
    ('USB-C Docking Station', '12-in-1 hub with dual HDMI and 100W PD',                 180.00, 40);

-- =============================================================
-- End of backup
-- =============================================================
