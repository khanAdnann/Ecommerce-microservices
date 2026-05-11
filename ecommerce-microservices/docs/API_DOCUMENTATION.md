# 📚 API Documentation - E-Commerce Microservices

## 🌐 API Gateway Endpoints

**Base URL**: `http://localhost:8080`

### Authentication
All protected endpoints require JWT token in `Authorization: Bearer <token>` header.

## 🔐 Authentication Service (Port 8081)

### POST /auth/register
Register a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890"
}
```

**Response:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "roles": ["ROLE_USER"],
  "enabled": true,
  "emailVerified": false,
  "createdAt": "2023-01-01T10:00:00"
}
```

### POST /auth/login
Authenticate user and return JWT tokens.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_USER"]
  }
}
```

### POST /auth/refresh
Refresh access token using refresh token.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### GET /auth/me
Get current authenticated user profile.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "profileImageUrl": "https://example.com/avatar.jpg",
  "roles": ["ROLE_USER"],
  "enabled": true,
  "emailVerified": true,
  "lastLogin": "2023-01-01T10:30:00"
}
```

### PUT /users/{id}
Update user profile.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "phoneNumber": "+1234567890",
  "profileImageUrl": "https://example.com/new-avatar.jpg"
}
```

### POST /users/{id}/change-password
Change user password.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "currentPassword": "OldPassword123",
  "newPassword": "NewPassword456"
}
```

## 🛍️ Product Service (Port 8082)

### GET /products
Get paginated list of products.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size
- `sort` (default: id) - Sort field
- `direction` (default: asc) - Sort direction
- `category` - Filter by category ID
- `search` - Search in name and description
- `minPrice` - Minimum price filter
- `maxPrice` - Maximum price filter
- `featured` - Filter featured products

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Laptop Pro 15\"",
      "description": "High-performance laptop with 15-inch display",
      "sku": "LP001",
      "price": 1299.99,
      "categoryId": 1,
      "brand": "TechBrand",
      "status": "ACTIVE",
      "featured": true,
      "rating": 4.5,
      "reviewCount": 25,
      "images": ["https://example.com/laptop1.jpg"],
      "createdAt": "2023-01-01T10:00:00"
    }
  ],
  "pageable": {
    "page": 0,
    "size": 10,
    "sort": "id,ASC"
  },
  "totalElements": 100,
  "totalPages": 10,
  "first": true,
  "last": false
}
```

### GET /products/{id}
Get product details by ID.

**Response:**
```json
{
  "id": 1,
  "name": "Laptop Pro 15\"",
  "description": "High-performance laptop with 15-inch display",
  "sku": "LP001",
  "price": 1299.99,
  "costPrice": 999.99,
  "categoryId": 1,
  "category": {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices and accessories"
  },
  "brand": "TechBrand",
  "weight": 2.5,
  "dimensions": "15 x 10 x 1 inches",
  "color": "Silver",
  "material": "Aluminum",
  "status": "ACTIVE",
  "featured": true,
  "rating": 4.5,
  "reviewCount": 25,
  "images": [
    "https://example.com/laptop1.jpg",
    "https://example.com/laptop2.jpg"
  ],
  "tags": ["laptop", "computer", "tech"],
  "attributes": [
    {
      "name": "Screen Size",
      "value": "15 inches"
    },
    {
      "name": "RAM",
      "value": "16GB"
    },
    {
      "name": "Storage",
      "value": "512GB SSD"
    }
  ],
  "createdAt": "2023-01-01T10:00:00",
  "updatedAt": "2023-01-01T10:00:00"
}
```

### GET /products/search
Search products with advanced filters.

**Query Parameters:**
- `q` - Search query
- `category` - Category filter
- `brand` - Brand filter
- `minPrice` - Minimum price
- `maxPrice` - Maximum price
- `rating` - Minimum rating
- `sortBy` - Sort option (price, rating, name, created)
- `page` - Page number
- `size` - Page size

### GET /products/categories
Get all product categories.

**Response:**
```json
[
  {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "parentId": null,
    "imageUrl": "https://example.com/electronics.jpg",
    "children": [
      {
        "id": 11,
        "name": "Laptops",
        "parentId": 1
      }
    ]
  }
]
```

### POST /products (Admin only)
Create new product.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "name": "New Product",
  "description": "Product description",
  "sku": "NP001",
  "price": 99.99,
  "categoryId": 1,
  "brand": "BrandName",
  "status": "ACTIVE",
  "featured": false,
  "images": ["https://example.com/product.jpg"],
  "attributes": [
    {
      "name": "Color",
      "value": "Red"
    }
  ]
}
```

## 🛒 Order Service (Port 8083)

### GET /orders
Get user's orders (requires authentication).

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
- `page` - Page number
- `size` - Page size
- `status` - Filter by status
- `fromDate` - Filter from date
- `toDate` - Filter to date

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "orderNumber": "ORD-001",
      "status": "DELIVERED",
      "totalAmount": 1299.99,
      "subtotal": 1299.99,
      "taxAmount": 0.00,
      "shippingAmount": 0.00,
      "currency": "USD",
      "paymentMethod": "CREDIT_CARD",
      "paymentStatus": "COMPLETED",
      "trackingNumber": "TRK001",
      "items": [
        {
          "id": 1,
          "productId": 1,
          "productName": "Laptop Pro 15\"",
          "productSku": "LP001",
          "quantity": 1,
          "unitPrice": 1299.99,
          "totalPrice": 1299.99
        }
      ],
      "shippingAddress": {
        "street": "123 Main St",
        "city": "New York",
        "state": "NY",
        "zipCode": "10001",
        "country": "USA"
      },
      "createdAt": "2023-01-01T10:00:00",
      "updatedAt": "2023-01-01T10:00:00",
      "shippedAt": "2023-01-02T10:00:00",
      "deliveredAt": "2023-01-04T10:00:00"
    }
  ],
  "pageable": {
    "page": 0,
    "size": 10
  },
  "totalElements": 5,
  "totalPages": 1
}
```

### GET /orders/{id}
Get order details by ID.

### POST /orders
Create new order.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 1
    }
  ],
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "paymentMethod": "CREDIT_CARD"
}
```

### PUT /orders/{id}/status (Admin only)
Update order status.

**Request Body:**
```json
{
  "status": "SHIPPED",
  "trackingNumber": "TRK001",
  "notes": "Order shipped via UPS"
}
```

## 💳 Payment Service (Port 8084)

### POST /payments/process
Process payment for an order.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "orderId": 1,
  "amount": 1299.99,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "cardDetails": {
    "cardNumber": "4111111111111111",
    "expiryMonth": 12,
    "expiryYear": 2024,
    "cvv": "123",
    "cardholderName": "John Doe"
  },
  "billingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }
}
```

**Response:**
```json
{
  "paymentReference": "PAY-001",
  "orderId": 1,
  "amount": 1299.99,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "status": "COMPLETED",
  "gatewayTransactionId": "TXN-12345",
  "processedAt": "2023-01-01T10:05:00"
}
```

### GET /payments/{reference}
Get payment details by reference.

### POST /payments/{reference}/refund
Refund payment.

**Request Body:**
```json
{
  "refundAmount": 1299.99,
  "refundReason": "Customer requested refund"
}
```

## 🛒 Cart Service (Port 8085)

### GET /cart
Get user's shopping cart.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "id": 1,
  "userId": 1,
  "status": "ACTIVE",
  "totalAmount": 149.97,
  "totalItems": 3,
  "items": [
    {
      "id": 1,
      "productId": 3,
      "productName": "Cotton T-Shirt",
      "productSku": "TS001",
      "quantity": 2,
      "unitPrice": 29.99,
      "totalPrice": 59.98,
      "addedAt": "2023-01-01T10:00:00"
    }
  ],
  "createdAt": "2023-01-01T10:00:00",
  "updatedAt": "2023-01-01T10:00:00"
}
```

### POST /cart/items
Add item to cart.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "productId": 1,
  "quantity": 1
}
```

### PUT /cart/items/{itemId}
Update cart item quantity.

**Request Body:**
```json
{
  "quantity": 2
}
```

### DELETE /cart/items/{itemId}
Remove item from cart.

### DELETE /cart
Clear entire cart.

## 📦 Inventory Service (Port 8086)

### GET /inventory/product/{productId}
Get inventory information for a product.

**Response:**
```json
{
  "id": 1,
  "productId": 1,
  "sku": "LP001",
  "quantityAvailable": 50,
  "quantityReserved": 5,
  "quantityOnOrder": 0,
  "reorderLevel": 10,
  "reorderQuantity": 25,
  "warehouseLocation": "WH-A-01-01",
  "lastUpdated": "2023-01-01T10:00:00"
}
```

### POST /inventory/reserve
Reserve inventory for an order.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 1
    }
  ],
  "orderId": 1
}
```

### POST /inventory/release
Release reserved inventory.

### GET /inventory/low-stock
Get products with low stock (Admin only).

## 🔔 Notification Service (Port 8087)

### GET /notifications
Get user's notifications.

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
- `page` - Page number
- `size` - Page size
- `status` - Filter by status
- `type` - Filter by type

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "type": "EMAIL",
      "category": "ORDER",
      "title": "Order Confirmation",
      "message": "Your order ORD-001 has been confirmed",
      "status": "SENT",
      "priority": "MEDIUM",
      "createdAt": "2023-01-01T10:00:00",
      "sentAt": "2023-01-01T10:01:00"
    }
  ],
  "pageable": {
    "page": 0,
    "size": 10
  },
  "totalElements": 25,
  "totalPages": 3
}
```

### PUT /notifications/{id}/read
Mark notification as read.

### GET /notifications/preferences
Get notification preferences.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "emailEnabled": true,
  "smsEnabled": false,
  "pushEnabled": true,
  "websocketEnabled": true,
  "orderNotifications": true,
  "paymentNotifications": true,
  "accountNotifications": true,
  "marketingNotifications": false,
  "systemNotifications": true
}
```

### PUT /notifications/preferences
Update notification preferences.

## ⭐ Review Service (Port 8088)

### GET /reviews/product/{productId}
Get reviews for a product.

**Query Parameters:**
- `page` - Page number
- `size` - Page size
- `rating` - Filter by rating
- `sort` - Sort option (helpful, recent, rating)

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "productId": 1,
      "userId": 1,
      "rating": 5,
      "title": "Excellent Laptop!",
      "reviewText": "This laptop exceeded my expectations...",
      "verifiedPurchase": true,
      "helpfulCount": 10,
      "status": "APPROVED",
      "createdAt": "2023-01-01T10:00:00",
      "user": {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe"
      },
      "images": [
        "https://example.com/review1.jpg"
      ]
    }
  ],
  "pageable": {
    "page": 0,
    "size": 10
  },
  "totalElements": 25,
  "totalPages": 3
}
```

### POST /reviews
Create new review.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "productId": 1,
  "rating": 5,
  "title": "Excellent Product!",
  "reviewText": "Great product, highly recommended!",
  "images": ["https://example.com/review.jpg"]
}
```

### PUT /reviews/{id}/helpful
Mark review as helpful.

### POST /reviews/{id}/report
Report inappropriate review.

## 📊 Analytics Service (Port 8089)

### GET /analytics/dashboard
Get dashboard analytics (Admin only).

**Response:**
```json
{
  "totalRevenue": 125000.50,
  "totalOrders": 450,
  "totalUsers": 1200,
  "conversionRate": 0.0350,
  "averageOrderValue": 277.78,
  "topProducts": [
    {
      "productId": 1,
      "productName": "Laptop Pro 15\"",
      "revenue": 25000.00,
      "quantity": 20
    }
  ],
  "recentOrders": [
    {
      "orderId": 1,
      "orderNumber": "ORD-001",
      "amount": 1299.99,
      "createdAt": "2023-01-01T10:00:00"
    }
  ]
}
```

### GET /analytics/sales
Get sales analytics.

**Query Parameters:**
- `fromDate` - Start date
- `toDate` - End date
- `groupBy` - Group by (day, week, month)

### GET /analytics/products
Get product performance analytics.

### GET /analytics/users
Get user activity analytics.

## 🔧 Error Responses

All APIs return consistent error responses:

```json
{
  "timestamp": "2023-01-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/users/register",
  "errors": {
    "email": "Email is required",
    "password": "Password must be at least 8 characters"
  }
}
```

## 📝 Rate Limiting

API endpoints are rate-limited to prevent abuse:
- **Public endpoints**: 100 requests per minute
- **Authenticated endpoints**: 1000 requests per minute
- **Admin endpoints**: 500 requests per minute

Rate limit headers are included in responses:
- `X-RateLimit-Limit` - Request limit
- `X-RateLimit-Remaining` - Remaining requests
- `X-RateLimit-Reset` - Reset time

## 🌐 Internationalization

API supports multiple languages via `Accept-Language` header:
- `en` - English (default)
- `es` - Spanish
- `fr` - French
- `de` - German

## 📚 SDK Examples

### JavaScript/Node.js
```javascript
const axios = require('axios');

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// Get products
const products = await api.get('/products');

// Create order
const order = await api.post('/orders', orderData);
```

### Python
```python
import requests

headers = {'Authorization': f'Bearer {token}'}

# Get products
products = requests.get('http://localhost:8080/products', headers=headers)

# Create order
order = requests.post('http://localhost:8080/orders', json=order_data, headers=headers)
```

### Java
```java
@RestController
public class ClientController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public List<Product> getProducts(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        return restTemplate.exchange(
            "http://localhost:8080/products",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<List<Product>>() {}
        ).getBody();
    }
}
```
