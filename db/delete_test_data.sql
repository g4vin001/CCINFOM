USE archers_ground_db;

-- Keep the original seeded/static records.
-- The seed file inserts transactional sample orders with order_id 1 to 19.
-- This script removes later test-generated transaction rows only.

DELETE FROM refunds
WHERE order_id > 19;

DELETE FROM order_status_log
WHERE order_id > 19;

DELETE FROM payments
WHERE order_id > 19;

DELETE FROM order_items
WHERE order_id > 19;

DELETE FROM orders
WHERE order_id > 19;

ALTER TABLE orders AUTO_INCREMENT = 20;
ALTER TABLE order_items AUTO_INCREMENT = 1;
ALTER TABLE payments AUTO_INCREMENT = 1;
ALTER TABLE order_status_log AUTO_INCREMENT = 1;
ALTER TABLE refunds AUTO_INCREMENT = 1;
