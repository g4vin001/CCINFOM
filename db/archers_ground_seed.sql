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
('Gate 1', 30.00, 'ACTIVE'),
('Gate 2', 35.00, 'ACTIVE'),
('Gate 3', 25.00, 'ACTIVE'),
('Gate 4', 40.00, 'ACTIVE'),
('North Gate', 45.00, 'ACTIVE'),
('South Gate', 45.00, 'ACTIVE'),
('Andrew Gate', 30.00, 'ACTIVE'),
('Razon Gate', 30.00, 'ACTIVE'),
('Library Gate', 25.00, 'INACTIVE'),
('Agno Gate', 35.00, 'ACTIVE');

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
('Americano', 'Coffee', TRUE, 110.00),
('Cafe Latte', 'Coffee', TRUE, 135.00),
('Spanish Latte', 'Coffee', TRUE, 145.00),
('Matcha Latte', 'Non-Coffee', TRUE, 150.00),
('Chocolate Muffin', 'Pastry', TRUE, 80.00),
('Blueberry Cheesecake', 'Dessert', TRUE, 160.00),
('Ham and Cheese Sandwich', 'Meal', TRUE, 175.00),
('Chicken Pesto Pasta', 'Meal', TRUE, 210.00),
('Iced Tea', 'Non-Coffee', TRUE, 75.00),
('Bottled Water', 'Others', TRUE, 35.00);

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
(1, 1, 1, 'CAMPUS_GATE_DELIVERY', 'DELIVERED', 250.00, 30.00, 280.00, '2026-02-03 09:15:00', '2026-02-03 09:28:00', '2026-02-03 09:50:00'),
(2, 5, NULL, 'PICK_UP', 'COMPLETED', 135.00, 0.00, 135.00, '2026-02-03 10:00:00', '2026-02-03 10:10:00', '2026-02-03 10:18:00'),
(6, 1, NULL, 'DINE_IN', 'COMPLETED', 285.00, 0.00, 285.00, '2026-02-04 11:30:00', '2026-02-04 11:42:00', '2026-02-04 12:05:00'),
(3, 1, 2, 'CAMPUS_GATE_DELIVERY', 'FAILED_DELIVERY', 150.00, 35.00, 185.00, '2026-02-04 13:00:00', '2026-02-04 13:18:00', '2026-02-04 13:50:00'),
(4, 5, NULL, 'PICK_UP', 'READY', 210.00, 0.00, 210.00, '2026-02-05 14:20:00', '2026-02-05 14:40:00', NULL),
(5, 1, 3, 'CAMPUS_GATE_DELIVERY', 'OUT_FOR_DELIVERY', 320.00, 25.00, 345.00, '2026-02-06 15:10:00', '2026-02-06 15:30:00', NULL),
(7, 5, NULL, 'DINE_IN', 'PAID', 145.00, 0.00, 145.00, '2026-02-07 16:00:00', NULL, NULL),
(8, 1, NULL, 'PICK_UP', 'REFUNDED', 160.00, 0.00, 160.00, '2026-02-08 17:00:00', NULL, NULL),
(9, 5, 4, 'CAMPUS_GATE_DELIVERY', 'PREPARING', 260.00, 40.00, 300.00, '2026-02-09 08:45:00', NULL, NULL),
(10, 1, NULL, 'DINE_IN', 'CANCELLED', 75.00, 0.00, 75.00, '2026-02-09 18:10:00', NULL, NULL);

INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price, line_subtotal) VALUES
(1, 2, 1, 135.00, 135.00),
(1, 5, 1, 80.00, 80.00),
(1, 10, 1, 35.00, 35.00),
(2, 2, 1, 135.00, 135.00),
(3, 7, 1, 175.00, 175.00),
(3, 9, 1, 75.00, 75.00),
(3, 10, 1, 35.00, 35.00),
(4, 4, 1, 150.00, 150.00),
(5, 8, 1, 210.00, 210.00),
(6, 3, 1, 145.00, 145.00),
(6, 7, 1, 175.00, 175.00),
(7, 3, 1, 145.00, 145.00),
(8, 6, 1, 160.00, 160.00),
(9, 1, 1, 110.00, 110.00),
(9, 4, 1, 150.00, 150.00),
(10, 9, 1, 75.00, 75.00);

INSERT INTO payments (order_id, payment_method, payment_amount, payment_status, payment_datetime) VALUES
(1, 'GCASH', 280.00, 'PAID', '2026-02-03 09:17:00'),
(2, 'GCASH', 135.00, 'PAID', '2026-02-03 10:01:00'),
(3, 'CASH', 285.00, 'PAID', '2026-02-04 11:31:00'),
(4, 'CARD', 185.00, 'PAID', '2026-02-04 13:01:00'),
(5, 'GCASH', 210.00, 'PAID', '2026-02-05 14:22:00'),
(6, 'GCASH', 345.00, 'PAID', '2026-02-06 15:12:00'),
(7, 'CARD', 145.00, 'PAID', '2026-02-07 16:01:00'),
(8, 'GCASH', 160.00, 'REFUNDED', '2026-02-08 17:01:00'),
(9, 'GCASH', 300.00, 'PAID', '2026-02-09 08:46:00'),
(10, 'CASH', 75.00, 'PAID', '2026-02-09 18:11:00');

INSERT INTO order_status_log (order_id, status, status_datetime, updated_by_employee_id, notes) VALUES
(1, 'READY', '2026-02-03 09:28:00', 3, 'Packed and ready for dispatch'),
(1, 'OUT_FOR_DELIVERY', '2026-02-03 09:35:00', 4, 'Courier left the cafe'),
(1, 'DELIVERED', '2026-02-03 09:50:00', 8, 'Received at Gate 1'),
(4, 'READY', '2026-02-04 13:18:00', 3, 'Prepared for delivery'),
(4, 'OUT_FOR_DELIVERY', '2026-02-04 13:25:00', 4, 'Courier assigned'),
(4, 'FAILED_DELIVERY', '2026-02-04 13:50:00', 8, 'Customer unavailable at gate'),
(6, 'READY', '2026-02-06 15:30:00', 3, 'Prepared for dispatch'),
(6, 'OUT_FOR_DELIVERY', '2026-02-06 15:40:00', 4, 'Courier on the way'),
(9, 'READY', '2026-02-09 09:05:00', 3, 'Awaiting pickup by rider');

INSERT INTO refunds (order_id, refund_amount, refund_reason, refund_datetime) VALUES
(8, 160.00, 'Cancelled by customer after payment', '2026-02-08 18:00:00');
