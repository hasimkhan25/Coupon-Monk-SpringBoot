# Coupons Management API (Spring Boot)

A clean, extensible REST API for **Cart-wise**, **Product-wise**, and **BxGy (Buy X Get Y)** coupons. Built with Spring Boot 3, Java 17, and H2 in-memory DB. Implements the Monk Commerce 2025 task.
> **Important**: The task emphasizes thinking through cases. Below, I list the cases I considered, which are also summarized in the README you are reading. Implementation focuses on core flows with clear **assumptions** and **limitations**. (Task brief: see attached PDF.)

---

## Quick Start

```bash
mvn spring-boot:run
# or
mvn clean package && java -jar target/coupons-0.0.1-SNAPSHOT.jar
```

H2 console: http://localhost:8080/h2 (JDBC URL: `jdbc:h2:mem:coupons`)

### Seeded Coupons

- **CART_WISE** `CART10_OVER_100`: 10% off carts over 100
- **PRODUCT_WISE** `PROD20_A`: 20% off productId=1
- **BXGY** `B2G1_XY_GET_Z_LIMIT2`: Buy 2 of [1,2] get 1 of [3] free, apply up to 2 times

> These correspond to examples in the task. 

---

## API Endpoints

- `POST /coupons` – Create a coupon
- `GET /coupons` – List coupons
- `GET /coupons/{id}` – Get coupon by id
- `PUT /coupons/{id}` – Update coupon
- `DELETE /coupons/{id}` – Delete coupon
- `POST /applicable-coupons` – Given a cart, returns each coupon with computed discount if applicable
- `POST /apply-coupon/{id}` – Applies a coupon, returning an **updated cart** with per-item discounts and totals

**Request/Response Examples** are aligned with the task brief and sample payloads.

---

## Data Model & Extensibility

- `Coupon { id, code, type, detailsJson, startsAt, endsAt, active }`
- `type` is an enum: `CART_WISE | PRODUCT_WISE | BXGY`
- `detailsJson` stores type-specific details. This makes it **easy to add new coupon types** without changing DB schema. Validators & calculators parse the JSON into typed detail classes.

### Details Schemas

#### Cart-wise
```json
{
  "threshold": 100,
  "percent": 10,
  "maxDiscount": 50 // optional
}
```

#### Product-wise
```json
{
  "products": [
    { "productId": 1, "percent": 20 },
    { "productId": 2, "percent": 15 }
  ]
}
```

#### BxGy
```json
{
  "buyProducts": [ {"productId": 1, "quantity": 2}, {"productId": 2, "quantity": 0} ],
  "getProducts": [ {"productId": 3, "quantity": 1} ],
  "repetitionLimit": 2
}
```
- `quantity` on a `buyProducts` entry contributes to **X per application**; the sum of entries (2 + 0 = 2) means **Buy 2** from the set `{1,2}`.
- Sum of `getProducts.quantity` defines **Y per application**; above: **Get 1** from set `{3}`.

---

## Core Assumptions

1. **No product catalog**: Prices come only from the cart payload (no DB of products). Free items in **BxGy** can only be applied to products that *already exist in the cart* (so we know their price). If none of the `getProducts` exist in the cart, the coupon is considered inapplicable **for discount computation**.
2. **BxGy selection strategy**: When multiple eligible "get" items are present, we choose the **most expensive items first** to maximize the customer's discount.
3. **Apply behavior for BxGy**: We mimic the task example by **increasing the quantity** of the chosen `get` items to include the free units, and then setting the line discount equal to `freeQty * unitPrice`.
4. **Rounding**: Discounts are rounded to 2 decimals (HALF_UP).
5. **Validity**: A coupon is applicable only if `active=true` and within `[startsAt, endsAt]` (if provided).

---

## Implemented Cases

- **Cart-wise threshold % discount** with optional cap.
- **Product-wise % discount** for a list of products (different percents per product).
- **BxGy Buy-X-Get-Y** across sets with repetition limit, handling mixed carts and selecting optimal discount.
- **Applicable Coupons endpoint** computes per-coupon discount given a cart, even if the coupon is not ultimately applicable (returns reason).
- **Apply Coupon endpoint** returns the updated cart with per-line discount and totals.
- **CRUD** for coupons.
- **Expiration** (`startsAt`, `endsAt`).

---

## Thought-through (Unimplemented / Partially Implemented) Cases

1. **Stacking rules**: Multiple coupons at once; precedence, combinability, and conflict resolution (not implemented).
2. **Min/Max item price constraints** for BxGy (e.g., free item max price cap).
3. **Per-customer limits**: usage count per user, per day, or per order.
4. **Exclusions**: brands/categories exclusion (requires product catalog).
5. **Fixed amount** discounts (₹ off) and tiered thresholds (e.g., 5% over 100, 10% over 200).
6. **Shipping/fees** inclusion or exclusion from cart total.
7. **Inventory checks** for freebies (requires product service).
8. **Currency & tax** handling beyond simple unit prices.

---

## Example Payloads

### POST /applicable-coupons
```json
{
  "items": [
    {"productId": 1, "quantity": 6, "price": 50},
    {"productId": 2, "quantity": 3, "price": 30},
    {"productId": 3, "quantity": 2, "price": 25}
  ]
}
```
**Example Response (abridged)**
```json
[
  {"couponId": 1, "code":"CART10_OVER_100", "type":"CART_WISE", "discount": 40.00, "reason":"Applicable"},
  {"couponId": 3, "code":"B2G1_XY_GET_Z_LIMIT2", "type":"BXGY", "discount": 50.00, "reason":"Applicable"}
]
```

### POST /apply-coupon/{id}
Using the same cart and coupon id `3` (BxGy above)
```json
{
  "items": [
    {"productId": 1, "quantity": 6, "price": 50},
    {"productId": 2, "quantity": 3, "price": 30},
    {"productId": 3, "quantity": 2, "price": 25}
  ]
}
```
**Example Response**
```json
{
  "items": [
    {"productId": 1, "quantity": 6, "price": 50, "totalDiscount": 0},
    {"productId": 2, "quantity": 3, "price": 30, "totalDiscount": 0},
    {"productId": 3, "quantity": 4, "price": 25, "totalDiscount": 50}
  ],
  "totalPrice": 490.00,
  "totalDiscount": 50.00,
  "finalPrice": 440.00
}
```

---

## Tests

See `src/test/java/com/monk/coupons/service/CouponServiceImplTest.java` for service-level unit tests of each calculator.

---

## Limitations

- **Freebies only for known prices** (existing cart lines). We do **not** add new line items to the cart because there is no product catalog to price them.
- **No authentication** / rate limiting / multitenancy.
- **No pagination** on coupon list.
- **No schema migrations** (H2 & `ddl-auto=update` for demo simplicity).

---

## How to Add a New Coupon Type

1. Create a new `Details` POJO under `service/calculators` for the type.
2. Add a calculator method in `Calculators` returning `Result`.
3. Extend `CouponType` enum.
4. Add switch cases in `CouponServiceImpl#calculateDiscount` and `#applyInternal`.
5. Define the expected JSON schema in the README; use it when creating coupons via `POST /coupons`.

---

## Notes

- Java 17, Spring Boot 3.3.x
- In-memory H2 for simplicity; swap to MySQL/Postgres by updating `application.yml` and adding the driver.
