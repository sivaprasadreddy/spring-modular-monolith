# Admin Feature Requirements

## Scope
Admin use cases for managing the product catalog and orders.
All admin actions require the user to be authenticated with `ROLE_ADMIN`.

---

## Status legend
| Status      | Meaning                     |
|-------------|-----------------------------|
| Pending     | Not yet started             |
| In Progress | Currently being implemented |
| Completed   | Implemented and tested      |

---

## Catalog Management

### UC-001 — List Products (Admin)
**Status:** Completed  
**Actor:** Admin  
**Description:** Admin can view a paginated list of all products including internal fields not shown to customers (e.g., creation metadata).  
**Pre-conditions:** User is authenticated as ADMIN.  
**Main flow:**
1. Admin navigates to `/admin/catalog/products`.
2. System returns a paginated list of products sorted by name, showing code, name, price, and description.
3. Admin can paginate through results.

---

### UC-002 — View Product Details (Admin)
**Status:** Completed  
**Actor:** Admin  
**Description:** Admin can view all details of a specific product.  
**Pre-conditions:** User is authenticated as ADMIN. Product with the given code exists.  
**Main flow:**
1. Admin navigates to `/admin/catalog/products/{code}`.
2. System returns full product details: code, name, description, imageUrl, price.

**Alternate flow:**
- If product does not exist, system returns 404 with a ProblemDetail response.

---

### UC-003 — Create Product
**Status:** Pending  
**Actor:** Admin  
**Description:** Admin can add a new product to the catalog.  
**Pre-conditions:** User is authenticated as ADMIN.  
**Main flow:**
1. Admin submits a `POST /admin/catalog/products` request with: code, name, description, imageUrl, price.
2. System validates that the product code is unique and all required fields are present.
3. System persists the product and returns the created product with HTTP 201.

**Alternate flows:**
- If code already exists, system returns 409 Conflict with a ProblemDetail response.
- If required fields are missing or invalid, system returns 400 with validation errors.

---

### UC-004 — Update Product
**Status:** Complete  
**Actor:** Admin  
**Description:** Admin can update the details of an existing product.  
**Pre-conditions:** User is authenticated as ADMIN. Product with the given code exists.  
**Main flow:**
1. Admin submits a `PUT /admin/catalog/products/{code}` request with updated fields: name, description, imageUrl, price.
2. System validates the request.
3. System persists the changes and returns the updated product.

**Alternate flows:**
- If product does not exist, system returns 404.
- If required fields are missing or invalid, system returns 400.

---

### UC-005 — Delete Product
**Status:** Complete  
**Actor:** Admin  
**Description:** Admin can remove a product from the catalog.  
**Pre-conditions:** User is authenticated as ADMIN. Product with the given code exists.  
**Main flow:**
1. Admin submits a `DELETE /admin/catalog/products/{code}` request.
2. System deletes the product and returns HTTP 204.

**Alternate flow:**
- If product does not exist, system returns 404.

---

## Order Management

### UC-006 — List All Orders (Admin)
**Status:** Complete  
**Actor:** Admin  
**Description:** Admin can view a paginated list of all orders across all customers, with optional filtering by status.  
**Pre-conditions:** User is authenticated as ADMIN.  
**Main flow:**
1. Admin submits a `GET /admin/orders` request with optional query parameters: `status`, `page`.
2. System returns a paginated list of orders sorted by creation date descending, including order number, customer name, status, and creation date.

---

### UC-007 — View Order Details (Admin)
**Status:** Completed  
**Actor:** Admin  
**Description:** Admin can view the full details of any order regardless of which customer placed it.  
**Pre-conditions:** User is authenticated as ADMIN. Order with the given order number exists.  
**Main flow:**
1. Admin submits a `GET /admin/orders/{orderNumber}` request.
2. System returns full order details: order number, customer info, delivery address, order item, status, timestamps.

**Alternate flow:**
- If order does not exist, system returns 404 with a ProblemDetail response.

---

### UC-008 — Update Order Status
**Status:** Completed  
**Actor:** Admin  
**Description:** Admin can update the status of an order (e.g., mark it as IN_PROCESS, DELIVERED, or CANCELLED).  
**Pre-conditions:** User is authenticated as ADMIN. Order exists. The requested status transition is valid (see rules below).  
**Valid status transitions:**
- `NEW` → `IN_PROCESS`, `CANCELLED`
- `IN_PROCESS` → `DELIVERED`, `CANCELLED`
- `DELIVERED` and `CANCELLED` are terminal — no further transitions allowed.

**Main flow:**
1. Admin submits a `PUT /admin/orders/{orderNumber}/status` request with `{"status": "<NEW_STATUS>"}`.
2. System validates the transition is permitted.
3. System persists the new status and returns the updated order.

**Alternate flows:**
- If order does not exist, system returns 404.
- If the transition is not permitted, system returns 400 with a ProblemDetail response describing the invalid transition.

---

## Inventory Management

### UC-009 — List Inventory
**Status:** Completed  
**Actor:** Admin  
**Description:** Admin can view the current stock level for all products.  
**Pre-conditions:** User is authenticated as ADMIN.  
**Main flow:**
1. Admin submits a `GET /admin/inventory` request with optional pagination parameters.
2. System returns a list of inventory records: productCode, quantity.

---

### UC-010 — Update Stock Level
**Status:** Completed  
**Actor:** Admin  
**Description:** Admin can set or adjust the stock quantity for a specific product.  
**Pre-conditions:** User is authenticated as ADMIN. A product with the given code exists in the catalog.  
**Main flow:**
1. Admin submits a `PUT /admin/inventory/{productCode}` request with `{"quantity": <value>}`.
2. System validates that quantity is non-negative.
3. System persists the new stock level and returns the updated inventory record.

**Alternate flows:**
- If no inventory record exists for the product code, system creates one.
- If quantity is negative, system returns 400.
