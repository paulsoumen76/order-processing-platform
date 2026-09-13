# Order Processing Platform — Spring Boot + Kafka

Three services demonstrate an event-driven flow:

```text
Client
  |
  v
Order Service :8081
  |
  | publishes OrderCreatedEvent
  v
Kafka topic: order.created
  |----------------------|
  v                      v
Inventory Service       Notification Service
:8082                    :8083
  |
  | publishes InventoryReservedEvent
  v
Kafka topic: inventory.reserved
  |
  v
Notification Service
```

## Services

- **order-service**: accepts orders and publishes `OrderCreatedEvent`.
- **inventory-service**: consumes `order.created`, reserves inventory, and publishes `InventoryReservedEvent`.
- **notification-service**: consumes both order and inventory events and logs notifications.

## Run

Prerequisites:
- Java 21
- Gradle 8+ (or use the Gradle wrapper if generated)
- Docker

Start Kafka:

```bash
docker compose up -d
```

Build:

```bash
./gradlew clean build
```

Run each service in separate terminals:

```bash
java -jar order-service/build/libs/order-service-1.0.0.jar
java -jar inventory-service/build/libs/inventory-service-1.0.0.jar
java -jar notification-service/build/libs/notification-service-1.0.0.jar
```

Create an order:

```bash
curl -X POST http://localhost:8081/orders   -H "Content-Type: application/json"   -d '{"productId":"IPHONE-17","quantity":2}'
```

Expected flow:

```text
POST /orders
   -> Order Service saves/creates order
   -> order.created
   -> Inventory Service reserves stock
   -> inventory.reserved
   -> Notification Service logs notification
```

This is intentionally a small learning project. For production, add a database, schema registry/Avro or Protobuf, idempotency, retries/DLT, observability, security, and an API gateway/service discovery as needed.


## Gradle commands

Build all services:

```bash
./gradlew clean build
```

Run a service directly:

```bash
./gradlew :order-service:bootRun
./gradlew :inventory-service:bootRun
./gradlew :notification-service:bootRun
```

On Windows:

```powershell
gradlew.bat clean build
```

## Project flow

1. Client calls `POST /orders` on Order Service.
2. Order Service publishes `OrderCreatedEvent` to `order.created`.
3. Inventory Service consumes `order.created`, reserves stock, and publishes `InventoryReservedEvent`.
4. Notification Service consumes order/inventory events and sends/logs notifications.

The services communicate through Kafka events rather than synchronous service-to-service HTTP calls.
