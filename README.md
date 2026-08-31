# Product Inventory & Stock Reservation
**Aldaleel Raqamee — Practical Technical Assessment**

A RESTful API built with **Spring Boot 3** and **PostgreSQL** that implements full product inventory management and atomic stock reservation for customer orders.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.0 |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Validation | Spring Validation (Jakarta) |
| Build Tool | Maven 3.9 |
| Container | Docker / Docker Compose |

---

## User Stories Implemented

| ID | Story | Status |
|---|---|---|
| US-001 | Admin manages products and stock quantities | ✅ Complete |
| US-002 | Customer reserves products when creating an order | ✅ Complete |
| US-003 | System releases reserved stock when an order is cancelled | ✅ Complete |

---

## Tasks Implemented

| Task | Description | Status |
|---|---|---|
| TASK-001 | Product model | ✅ |
| TASK-002 | CRUD endpoints | ✅ |
| TASK-003 | Stock quantity | ✅ |
| TASK-004 | Validation | ✅ |
| TASK-005 | Pagination | ✅ |
| TASK-006 | Create order items | ✅ |
| TASK-007 | Validate stock | ✅ |
| TASK-008 | Reserve stock | ✅ |
| TASK-009 | Prevent negative stock | ✅ |
| TASK-010 | Handle concurrent requests (PESSIMISTIC_WRITE lock) | ✅ |
| TASK-011 | Rollback failed operations (@Transactional) | ✅ |
| TASK-012 | Implement cancellation | ✅ |
| TASK-013 | Restore stock | ✅ |
| TASK-014 | Prevent double release | ✅ |
| TASK-015 | Handle invalid state transitions | ✅ |

---

## Project Structure

```
src/main/java/com/aldaleel/inventory/
├── InventoryApplication.java
├── common/
│   ├── exception/
│   │   ├── ResourceNotFoundException.java       # 404
│   │   ├── DuplicateResourceException.java      # 409
│   │   ├── InsufficientStockException.java      # 409
│   │   └── InvalidOrderStateException.java      # 422
│   └── handler/
│       ├── GlobalExceptionHandler.java
│       ├── ErrorResponse.java
│       └── ValidationErrorResponse.java
├── product/
│   ├── entity/Product.java
│   ├── dto/ProductRequest.java
│   ├── dto/ProductResponse.java
│   ├── dto/StockUpdateRequest.java
│   ├── repository/ProductRepository.java
│   ├── service/ProductService.java
│   └── controller/ProductController.java
└── order/
    ├── entity/Order.java
    ├── entity/OrderItem.java
    ├── entity/OrderStatus.java
    ├── dto/OrderRequest.java
    ├── dto/OrderItemRequest.java
    ├── dto/OrderResponse.java
    ├── dto/OrderItemResponse.java
    ├── repository/OrderRepository.java
    ├── service/OrderService.java
    └── controller/OrderController.java
```

---

## Setup & Run Instructions

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 16 (or Docker)

---

### Option A — Run with Docker (recommended)

**1. Start PostgreSQL:**
```bash
docker-compose up -d
```
This automatically creates the `aldaleel_inventory` database.

**2. Run the application:**
```bash
mvn spring-boot:run
```

---

### Option B — Run with local PostgreSQL

**1. Create the database:**
```sql
CREATE DATABASE aldaleel_inventory;
```

**2. Configure credentials:**

Copy `.env.example` to `.env` and fill in your values:
```bash
cp .env.example .env
```

Edit `.env`:
```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=aldaleel_inventory
DB_USERNAME=postgres
DB_PASSWORD=your_password
```

**3. Run the application:**
```bash
mvn spring-boot:run
```

The app starts on **http://localhost:8080**.  
Hibernate will auto-create all tables on first run (`ddl-auto=update`).

---

### Option C — Run as JAR

```bash
mvn clean package -DskipTests
java -jar target/inventory-1.0.0.jar
```

---

## API Endpoints

### Products — `/api/v1/products`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/products` | List all products (paginated) |
| GET | `/api/v1/products/{id}` | Get product by ID |
| POST | `/api/v1/products` | Create a new product |
| PUT | `/api/v1/products/{id}` | Update a product |
| PATCH | `/api/v1/products/{id}/stock` | Update stock quantity only |
| DELETE | `/api/v1/products/{id}` | Delete a product |

### Orders — `/api/v1/orders`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/orders` | List all orders (paginated) |
| GET | `/api/v1/orders/{id}` | Get order by ID |
| POST | `/api/v1/orders` | Create order & reserve stock |
| PATCH | `/api/v1/orders/{id}/cancel` | Cancel order & restore stock |

---

## Request & Response Examples

### Create Product
```http
POST /api/v1/products
Content-Type: application/json

{
  "name": "Dell XPS 15 Laptop",
  "description": "High-performance laptop",
  "price": 1200.00,
  "stockQuantity": 50
}
```

### Create Order (reserves stock)
```http
POST /api/v1/orders
Content-Type: application/json

{
  "items": [
    { "productId": 1, "quantity": 3 }
  ]
}
```

### Cancel Order (restores stock)
```http
PATCH /api/v1/orders/1/cancel
```

---

## Error Responses

All errors follow a consistent envelope:

```json
{
  "timestamp": "2026-08-30T10:15:30",
  "status": 409,
  "error": "Conflict",
  "message": "Insufficient stock for product 'Dell XPS 15 Laptop': requested 999, available 50",
  "path": "/api/v1/orders"
}
```

Validation errors (400) include a per-field breakdown:

```json
{
  "timestamp": "2026-08-30T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed. Check the 'errors' field for details.",
  "path": "/api/v1/products",
  "errors": {
    "name": "Product name is required",
    "price": "Price is required"
  }
}
```

---

## Concurrency & Data Integrity

- **TASK-010**: Stock updates use `PESSIMISTIC_WRITE` database-level locks to serialise concurrent order requests for the same product — preventing race conditions.
- **TASK-011**: Every order creation and cancellation runs inside a single `@Transactional` boundary — any failure triggers a full rollback, leaving inventory in a consistent state.
- **TASK-009**: Stock quantity is validated before deduction and the `@Min(0)` constraint prevents it from ever going negative.

---

## Order State Machine

```
PENDING ──► CONFIRMED ──► DELIVERED  (terminal)
   │             │
   └─────────────┴──► CANCELLED  (terminal)
```

Attempting to cancel a `DELIVERED` or already `CANCELLED` order returns `422 Unprocessable Entity`.

---

## Database Backup

A SQL dump of the database schema is included in:
```
database/aldaleel_inventory_backup.sql
```

To restore:
```bash
psql -U postgres -d aldaleel_inventory -f database/aldaleel_inventory_backup.sql
```

---

## Assumptions & Notes

- Authentication and authorization are out of scope for this assessment. All endpoints are publicly accessible.
- The `ddl-auto=update` setting is used for simplicity. In production this would be replaced with Flyway or Liquibase migrations.
- Price is captured as a snapshot on each `OrderItem` at creation time, so later product price changes do not affect existing orders.
- The `CONFIRMED` and `DELIVERED` status transitions are modelled but no dedicated endpoints are implemented for them, as they were not part of the specified user stories.
