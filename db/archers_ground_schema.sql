CREATE DATABASE IF NOT EXISTS archers_ground_db;
USE archers_ground_db;

CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    last_name VARCHAR(60) NOT NULL,
    first_name VARCHAR(60) NOT NULL,
    customer_type ENUM('LASALLIAN', 'NON_LASALLIAN') NOT NULL,
    dlsu_id_number VARCHAR(20),
    identifier_email VARCHAR(100),
    date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_customer_eligibility
        CHECK (
            (customer_type = 'LASALLIAN' AND dlsu_id_number IS NOT NULL)
            OR customer_type = 'NON_LASALLIAN'
        )
);

CREATE TABLE gates (
    gate_id INT PRIMARY KEY AUTO_INCREMENT,
    gate_name VARCHAR(80) NOT NULL UNIQUE,
    delivery_fee DECIMAL(10, 2) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE employees (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    last_name VARCHAR(60) NOT NULL,
    first_name VARCHAR(60) NOT NULL,
    role VARCHAR(40) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    date_hired DATE NOT NULL
);

CREATE TABLE menu_items (
    menu_item_id INT PRIMARY KEY AUTO_INCREMENT,
    item_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    processed_by_employee_id INT,
    gate_id INT,
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
    prepared_at DATETIME,
    completed_at DATETIME,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (processed_by_employee_id) REFERENCES employees(employee_id),
    FOREIGN KEY (gate_id) REFERENCES gates(gate_id)
);

CREATE TABLE order_items (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    line_subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(menu_item_id)
);

CREATE TABLE payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL UNIQUE,
    payment_method ENUM('CASH', 'GCASH', 'CARD') NOT NULL,
    payment_amount DECIMAL(10, 2) NOT NULL,
    payment_status ENUM('PENDING', 'PAID', 'REFUNDED') NOT NULL DEFAULT 'PAID',
    payment_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

CREATE TABLE order_status_log (
    status_log_id INT PRIMARY KEY AUTO_INCREMENT,
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
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (updated_by_employee_id) REFERENCES employees(employee_id)
);

CREATE TABLE refunds (
    refund_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL UNIQUE,
    refund_amount DECIMAL(10, 2) NOT NULL,
    refund_reason VARCHAR(255) NOT NULL,
    refund_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);
