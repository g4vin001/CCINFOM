CREATE DATABASE IF NOT EXISTS archers_ground_db;
USE archers_ground_db;

CREATE TABLE customers (
    customer_id INT NOT NULL AUTO_INCREMENT,
    last_name VARCHAR(60) NOT NULL,
    first_name VARCHAR(60) NOT NULL,
    customer_type ENUM('LASALLIAN', 'NON_LASALLIAN') NOT NULL,
    dlsu_id_number VARCHAR(20),
    identifier_email VARCHAR(100),
    date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id),
    CONSTRAINT chk_customer_eligibility
        CHECK (
            (customer_type = 'LASALLIAN' AND dlsu_id_number IS NOT NULL)
            OR customer_type = 'NON_LASALLIAN'
        )
) ENGINE = InnoDB;

CREATE TABLE gates (
    gate_id INT NOT NULL AUTO_INCREMENT,
    gate_name VARCHAR(80) NOT NULL,
    delivery_fee DECIMAL(10, 2) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (gate_id),
    UNIQUE KEY uq_gates_gate_name (gate_name)
) ENGINE = InnoDB;

CREATE TABLE employees (
    employee_id INT NOT NULL AUTO_INCREMENT,
    last_name VARCHAR(60) NOT NULL,
    first_name VARCHAR(60) NOT NULL,
    role VARCHAR(40) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    date_hired DATE NOT NULL,
    PRIMARY KEY (employee_id)
) ENGINE = InnoDB;

CREATE TABLE menu_items (
    menu_item_id INT NOT NULL AUTO_INCREMENT,
    item_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT chk_menu_item_price CHECK (price >= 0),
    PRIMARY KEY (menu_item_id)
) ENGINE = InnoDB;

CREATE TABLE orders (
    order_id INT NOT NULL AUTO_INCREMENT,
    customer_id INT NOT NULL,
    processed_by_employee_id INT NULL,
    gate_id INT NULL,
    order_type ENUM('DINE_IN', 'PICK_UP', 'CAMPUS_GATE_DELIVERY') NOT NULL,
    order_status ENUM(
        'CREATED',
        'PAID',
        'PREPARING',
        'READY',
        'OUT_FOR_DELIVERY',
        'DELIVERED',
        'COMPLETED',
        'CANCELLED',
        'REFUNDED',
        'FAILED_DELIVERY'
    ) NOT NULL DEFAULT 'CREATED',
    subtotal_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    delivery_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    order_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    prepared_at DATETIME NULL,
    completed_at DATETIME NULL,
    CONSTRAINT chk_orders_subtotal CHECK (subtotal_amount >= 0),
    CONSTRAINT chk_orders_delivery_fee CHECK (delivery_fee >= 0),
    CONSTRAINT chk_orders_total_amount CHECK (total_amount >= 0),
    PRIMARY KEY (order_id),
    KEY idx_orders_customer_id (customer_id),
    KEY idx_orders_employee_id (processed_by_employee_id),
    KEY idx_orders_gate_id (gate_id),
    KEY idx_orders_order_datetime (order_datetime),
    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_orders_employee
        FOREIGN KEY (processed_by_employee_id) REFERENCES employees(employee_id),
    CONSTRAINT fk_orders_gate
        FOREIGN KEY (gate_id) REFERENCES gates(gate_id)
) ENGINE = InnoDB;

CREATE TABLE order_items (
    order_item_id INT NOT NULL AUTO_INCREMENT,
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    line_subtotal DECIMAL(10, 2) NOT NULL,
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_items_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_order_items_line_subtotal CHECK (line_subtotal >= 0),
    PRIMARY KEY (order_item_id),
    KEY idx_order_items_order_id (order_id),
    KEY idx_order_items_menu_item_id (menu_item_id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_order_items_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items(menu_item_id)
) ENGINE = InnoDB;

CREATE TABLE payments (
    payment_id INT NOT NULL AUTO_INCREMENT,
    order_id INT NOT NULL,
    payment_method ENUM('CASH', 'GCASH', 'CARD') NOT NULL,
    payment_amount DECIMAL(10, 2) NOT NULL,
    payment_status ENUM('PENDING', 'PAID', 'REFUNDED') NOT NULL DEFAULT 'PAID',
    payment_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_payments_amount CHECK (payment_amount >= 0),
    PRIMARY KEY (payment_id),
    UNIQUE KEY uq_payments_order_id (order_id),
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
) ENGINE = InnoDB;

CREATE TABLE order_status_log (
    status_log_id INT NOT NULL AUTO_INCREMENT,
    order_id INT NOT NULL,
    status ENUM(
        'PAID',
        'READY',
        'OUT_FOR_DELIVERY',
        'DELIVERED',
        'COMPLETED',
        'CANCELLED',
        'REFUNDED',
        'FAILED_DELIVERY'
    ) NOT NULL,
    status_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by_employee_id INT NOT NULL,
    notes VARCHAR(255),
    PRIMARY KEY (status_log_id),
    KEY idx_order_status_log_order_id (order_id),
    KEY idx_order_status_log_employee_id (updated_by_employee_id),
    CONSTRAINT fk_order_status_log_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_order_status_log_employee
        FOREIGN KEY (updated_by_employee_id) REFERENCES employees(employee_id)
) ENGINE = InnoDB;

CREATE TABLE refunds (
    refund_id INT NOT NULL AUTO_INCREMENT,
    order_id INT NOT NULL,
    payment_id INT NOT NULL,
    refund_amount DECIMAL(10, 2) NOT NULL,
    refund_reason VARCHAR(255) NOT NULL,
    refund_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_refunds_amount CHECK (refund_amount >= 0),
    PRIMARY KEY (refund_id),
    UNIQUE KEY uq_refunds_order_id (order_id),
    KEY idx_refunds_payment_id (payment_id),
    CONSTRAINT fk_refunds_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_refunds_payment
        FOREIGN KEY (payment_id) REFERENCES payments(payment_id)
) ENGINE = InnoDB;

USE archers_ground_db;

INSERT INTO customers (last_name, first_name, customer_type, dlsu_id_number, identifier_email) VALUES
('Alonzo', 'Mika', 'LASALLIAN', '12345678', 'mika.alonzo@dlsu.edu.ph'),
('Santos', 'Paolo', 'LASALLIAN', '12345679', 'paolo.santos@dlsu.edu.ph'),
('Rivera', 'Anne', 'LASALLIAN', '12345680', 'anne.rivera@dlsu.edu.ph'),
('Lee', 'Marcus', 'LASALLIAN', '12345681', 'marcus.lee@dlsu.edu.ph'),
('Tan', 'Jill', 'LASALLIAN', '12345682', 'jill.tan@dlsu.edu.ph'),
('Cruz', 'Ralph', 'NON_LASALLIAN', NULL, 'ralph.cruz@email.com'),
('Ong', 'Lara', 'NON_LASALLIAN', NULL, 'lara.ong@email.com'),
('Sy', 'Kevin', 'NON_LASALLIAN', NULL, 'kevin.sy@email.com'),
('Mendoza', 'Iris', 'LASALLIAN', '12345683', 'iris.mendoza@dlsu.edu.ph'),
('Lim', 'Noah', 'NON_LASALLIAN', NULL, 'noah.lim@email.com');

INSERT INTO gates (gate_name, delivery_fee, status) VALUES
('Gate 1 - South Gate', 30.00, 'ACTIVE'),
('Gate 2 - North Gate', 30.00, 'ACTIVE'),
('Gate 3 - Velasco Gate', 30.00, 'ACTIVE'),
('Gate 4A - Gokongwei Gate', 35.00, 'ACTIVE'),
('Gate 4B - Gokongwei Gate', 35.00, 'ACTIVE'),
('Gate 5A - Andrew Gate', 45.00, 'ACTIVE'),
('Gate 5B - Andrew Gate', 45.00, 'ACTIVE'),
('Gate 6 - Razon Gate', 45.00, 'ACTIVE'),
('Gate 7 - STRC', 40.00, 'ACTIVE');

INSERT INTO employees (last_name, first_name, role, status, date_hired) VALUES
('Reyes', 'Ina', 'Cashier', 'ACTIVE', '2024-06-01'),
('Flores', 'Niko', 'Barista', 'ACTIVE', '2024-06-15'),
('Panganiban', 'Elle', 'Kitchen Staff', 'ACTIVE', '2024-07-01'),
('Chua', 'Mark', 'Delivery Coordinator', 'ACTIVE', '2024-07-10'),
('Yu', 'Jessa', 'Cashier', 'ACTIVE', '2024-08-01'),
('Go', 'Andre', 'Barista', 'ACTIVE', '2024-08-05'),
('Uy', 'Dana', 'Kitchen Staff', 'ACTIVE', '2024-08-12'),
('Torres', 'Ben', 'Delivery Rider', 'ACTIVE', '2024-09-01'),
('Bautista', 'Mara', 'Supervisor', 'ACTIVE', '2024-09-15'),
('Lopez', 'Carl', 'Cashier', 'INACTIVE', '2024-10-01');

INSERT INTO menu_items (item_name, category, is_available, price) VALUES
('Espresso', 'Hot', TRUE, 90.00),
('Americano', 'Hot', TRUE, 110.00),
('Caffe Latte', 'Hot', TRUE, 135.00),
('Caffe Mocha', 'Hot', TRUE, 145.00),
('Cappuccino', 'Hot', TRUE, 140.00),
('Archer''s Latte', 'Hot', TRUE, 150.00),
('Iced Archer''s Latte', 'Cold', TRUE, 155.00),
('Verde Cold Brew', 'Cold', TRUE, 145.00),
('Spanish Latte', 'Cold', TRUE, 150.00),
('Iced Mocha', 'Cold', TRUE, 145.00),
('Green Apple Sparkling Juice', 'Non Coffee', TRUE, 120.00),
('Animotea', 'Non Coffee', TRUE, 110.00),
('Green Flag Matcha', 'Non Coffee', TRUE, 150.00),
('Taft Cooler', 'Non Coffee', TRUE, 125.00),
('Strawberry Semester', 'Non Coffee', TRUE, 135.00),
('Iced Chocolate', 'Non Coffee', TRUE, 130.00),
('All-Nighter Grilled Cheese', 'Snacks', TRUE, 160.00),
('Thesis Tuna Melt', 'Snacks', TRUE, 175.00),
('Campus Club Sandwich', 'Snacks', TRUE, 185.00),
('Dean''s List Croissant', 'Snacks', TRUE, 140.00),
('Carbonara', 'Snacks', TRUE, 210.00);

INSERT INTO orders (
    customer_id,
    processed_by_employee_id,
    gate_id,
    order_type,
    order_status,
    subtotal_amount,
    delivery_fee,
    total_amount,
    order_datetime,
    prepared_at,
    completed_at
) VALUES
(1, 1, 1, 'CAMPUS_GATE_DELIVERY', 'DELIVERED', 295.00, 30.00, 325.00, '2026-02-03 09:15:00', '2026-02-03 09:28:00', '2026-02-03 09:50:00'),
(2, 5, NULL, 'PICK_UP', 'COMPLETED', 110.00, 0.00, 110.00, '2026-02-03 10:00:00', '2026-02-03 10:10:00', '2026-02-03 10:18:00'),
(6, 1, NULL, 'DINE_IN', 'COMPLETED', 315.00, 0.00, 315.00, '2026-02-04 11:30:00', '2026-02-04 11:42:00', '2026-02-04 12:05:00'),
(3, 1, 2, 'CAMPUS_GATE_DELIVERY', 'FAILED_DELIVERY', 150.00, 30.00, 180.00, '2026-02-04 13:00:00', '2026-02-04 13:18:00', '2026-02-04 13:50:00'),
(4, 5, NULL, 'PICK_UP', 'READY', 210.00, 0.00, 210.00, '2026-02-05 14:20:00', '2026-02-05 14:40:00', NULL),
(5, 1, 3, 'CAMPUS_GATE_DELIVERY', 'OUT_FOR_DELIVERY', 325.00, 30.00, 355.00, '2026-02-06 15:10:00', '2026-02-06 15:30:00', NULL),
(7, 5, NULL, 'DINE_IN', 'PAID', 145.00, 0.00, 145.00, '2026-02-07 16:00:00', NULL, NULL),
(8, 1, NULL, 'PICK_UP', 'REFUNDED', 150.00, 0.00, 150.00, '2026-02-08 17:00:00', NULL, NULL),
(9, 5, 4, 'CAMPUS_GATE_DELIVERY', 'PREPARING', 240.00, 35.00, 275.00, '2026-02-09 08:45:00', NULL, NULL),
(10, 1, NULL, 'DINE_IN', 'CANCELLED', 110.00, 0.00, 110.00, '2026-02-09 18:10:00', NULL, NULL);

INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price, line_subtotal) VALUES
(1, 3, 1, 135.00, 135.00),
(1, 17, 1, 160.00, 160.00),
(2, 2, 1, 110.00, 110.00),
(3, 19, 1, 185.00, 185.00),
(3, 16, 1, 130.00, 130.00),
(4, 13, 1, 150.00, 150.00),
(5, 21, 1, 210.00, 210.00),
(6, 9, 1, 150.00, 150.00),
(6, 18, 1, 175.00, 175.00),
(7, 8, 1, 145.00, 145.00),
(8, 6, 1, 150.00, 150.00),
(9, 1, 1, 90.00, 90.00),
(9, 13, 1, 150.00, 150.00),
(10, 12, 1, 110.00, 110.00);

INSERT INTO payments (order_id, payment_method, payment_amount, payment_status, payment_datetime) VALUES
(1, 'GCASH', 325.00, 'PAID', '2026-02-03 09:17:00'),
(2, 'GCASH', 110.00, 'PAID', '2026-02-03 10:01:00'),
(3, 'CASH', 315.00, 'PAID', '2026-02-04 11:31:00'),
(4, 'CARD', 180.00, 'PAID', '2026-02-04 13:01:00'),
(5, 'GCASH', 210.00, 'PAID', '2026-02-05 14:22:00'),
(6, 'GCASH', 355.00, 'PAID', '2026-02-06 15:12:00'),
(7, 'CARD', 145.00, 'PAID', '2026-02-07 16:01:00'),
(8, 'GCASH', 150.00, 'REFUNDED', '2026-02-08 17:01:00'),
(9, 'GCASH', 275.00, 'PAID', '2026-02-09 08:46:00'),
(10, 'CASH', 110.00, 'PAID', '2026-02-09 18:11:00');

INSERT INTO order_status_log (order_id, status, status_datetime, updated_by_employee_id, notes) VALUES
(1, 'READY', '2026-02-03 09:28:00', 3, 'Packed and ready for dispatch'),
(1, 'OUT_FOR_DELIVERY', '2026-02-03 09:35:00', 4, 'Courier left the cafe'),
(1, 'DELIVERED', '2026-02-03 09:50:00', 8, 'Received at Gate 1 - South Gate'),
(4, 'READY', '2026-02-04 13:18:00', 3, 'Prepared for delivery'),
(4, 'OUT_FOR_DELIVERY', '2026-02-04 13:25:00', 4, 'Courier assigned'),
(4, 'FAILED_DELIVERY', '2026-02-04 13:50:00', 8, 'Customer unavailable at gate'),
(6, 'READY', '2026-02-06 15:30:00', 3, 'Prepared for dispatch'),
(6, 'OUT_FOR_DELIVERY', '2026-02-06 15:40:00', 4, 'Courier on the way'),
(9, 'READY', '2026-02-09 09:05:00', 3, 'Awaiting pickup by rider'),
(10, 'CANCELLED', '2026-02-09 18:12:00', 1, 'Order cancelled before preparation started');

INSERT INTO orders (
    customer_id,
    processed_by_employee_id,
    gate_id,
    order_type,
    order_status,
    subtotal_amount,
    delivery_fee,
    total_amount,
    order_datetime,
    prepared_at,
    completed_at
) VALUES
(1, 5, NULL, 'PICK_UP', 'REFUNDED', 90.00, 0.00, 90.00, '2026-02-10 08:20:00', '2026-02-10 08:30:00', NULL),
(2, 1, 5, 'CAMPUS_GATE_DELIVERY', 'REFUNDED', 285.00, 35.00, 320.00, '2026-02-10 10:40:00', '2026-02-10 11:00:00', NULL),
(3, 5, NULL, 'DINE_IN', 'REFUNDED', 145.00, 0.00, 145.00, '2026-02-10 12:15:00', '2026-02-10 12:28:00', NULL),
(4, 1, 6, 'CAMPUS_GATE_DELIVERY', 'REFUNDED', 160.00, 45.00, 205.00, '2026-02-11 09:10:00', '2026-02-11 09:26:00', NULL),
(5, 5, NULL, 'PICK_UP', 'REFUNDED', 140.00, 0.00, 140.00, '2026-02-11 14:05:00', '2026-02-11 14:20:00', NULL),
(6, 1, NULL, 'DINE_IN', 'REFUNDED', 210.00, 0.00, 210.00, '2026-02-12 16:30:00', '2026-02-12 16:48:00', NULL),
(7, 5, 8, 'CAMPUS_GATE_DELIVERY', 'REFUNDED', 125.00, 45.00, 170.00, '2026-02-13 11:25:00', '2026-02-13 11:40:00', NULL),
(8, 1, NULL, 'PICK_UP', 'REFUNDED', 135.00, 0.00, 135.00, '2026-02-14 15:45:00', '2026-02-14 16:00:00', NULL),
(9, 5, 2, 'CAMPUS_GATE_DELIVERY', 'REFUNDED', 150.00, 30.00, 180.00, '2026-02-15 18:05:00', '2026-02-15 18:22:00', NULL);

INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price, line_subtotal) VALUES
(11, 1, 1, 90.00, 90.00),
(12, 2, 1, 110.00, 110.00),
(12, 18, 1, 175.00, 175.00),
(13, 10, 1, 145.00, 145.00),
(14, 17, 1, 160.00, 160.00),
(15, 20, 1, 140.00, 140.00),
(16, 21, 1, 210.00, 210.00),
(17, 14, 1, 125.00, 125.00),
(18, 3, 1, 135.00, 135.00),
(19, 13, 1, 150.00, 150.00);

INSERT INTO payments (order_id, payment_method, payment_amount, payment_status, payment_datetime) VALUES
(11, 'CASH', 90.00, 'REFUNDED', '2026-02-10 08:21:00'),
(12, 'GCASH', 320.00, 'REFUNDED', '2026-02-10 10:42:00'),
(13, 'CARD', 145.00, 'REFUNDED', '2026-02-10 12:16:00'),
(14, 'GCASH', 205.00, 'REFUNDED', '2026-02-11 09:11:00'),
(15, 'CASH', 140.00, 'REFUNDED', '2026-02-11 14:06:00'),
(16, 'CARD', 210.00, 'REFUNDED', '2026-02-12 16:31:00'),
(17, 'GCASH', 170.00, 'REFUNDED', '2026-02-13 11:26:00'),
(18, 'CARD', 135.00, 'REFUNDED', '2026-02-14 15:46:00'),
(19, 'GCASH', 180.00, 'REFUNDED', '2026-02-15 18:06:00');

INSERT INTO order_status_log (order_id, status, status_datetime, updated_by_employee_id, notes) VALUES
(11, 'REFUNDED', '2026-02-10 08:50:00', 1, 'Refunded after duplicate payment entry'),
(12, 'REFUNDED', '2026-02-10 11:30:00', 4, 'Customer requested cancellation before handoff'),
(13, 'REFUNDED', '2026-02-10 12:50:00', 5, 'Drink remade incorrectly and payment reversed'),
(14, 'REFUNDED', '2026-02-11 10:05:00', 8, 'Delivery could not proceed due to gate access issue'),
(15, 'REFUNDED', '2026-02-11 14:45:00', 5, 'Customer did not claim pickup order'),
(16, 'REFUNDED', '2026-02-12 17:20:00', 1, 'Item became unavailable after payment'),
(17, 'REFUNDED', '2026-02-13 12:10:00', 4, 'Delivery address was changed too late to fulfill'),
(18, 'REFUNDED', '2026-02-14 16:25:00', 5, 'Pickup order cancelled by customer'),
(19, 'REFUNDED', '2026-02-15 18:55:00', 8, 'Driver reported unsuccessful gate meetup');

INSERT INTO refunds (order_id, payment_id, refund_amount, refund_reason, refund_datetime) VALUES
(8, 8, 150.00, 'Cancelled by customer after payment', '2026-02-08 18:00:00'),
(11, 11, 90.00, 'Duplicate payment entry corrected', '2026-02-10 08:50:00'),
(12, 12, 320.00, 'Cancelled before dispatch to campus gate', '2026-02-10 11:30:00'),
(13, 13, 145.00, 'Incorrect drink preparation', '2026-02-10 12:50:00'),
(14, 14, 205.00, 'Gate access issue prevented delivery', '2026-02-11 10:05:00'),
(15, 15, 140.00, 'Unclaimed pickup order', '2026-02-11 14:45:00'),
(16, 16, 210.00, 'Ordered item became unavailable', '2026-02-12 17:20:00'),
(17, 17, 170.00, 'Delivery details changed after preparation', '2026-02-13 12:10:00'),
(18, 18, 135.00, 'Customer cancelled pickup order', '2026-02-14 16:25:00'),
(19, 19, 180.00, 'Unsuccessful gate meetup', '2026-02-15 18:55:00');
