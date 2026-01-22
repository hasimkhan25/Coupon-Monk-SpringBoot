INSERT INTO coupons (id, code, type, details_json, starts_at, ends_at, active) VALUES
(1, 'CART10_OVER_100', 'CART_WISE', '{"threshold":100, "percent":10}', NULL, NULL, TRUE),
(2, 'PROD20_A', 'PRODUCT_WISE', '{"products":[{"productId":1, "percent":20}]}' , NULL, NULL, TRUE),
(3, 'B2G1_XY_GET_Z_LIMIT2', 'BXGY', '{"buyProducts":[{"productId":1, "quantity":2},{"productId":2, "quantity":0}], "getProducts":[{"productId":3, "quantity":1}], "repetitionLimit":2}', NULL, NULL, TRUE);
