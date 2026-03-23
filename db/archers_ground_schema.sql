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
