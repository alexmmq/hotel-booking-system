## Run order

1) Eureka:
    - cd eureka-server
    - mvn spring-boot:run

2) Hotel Service:
    - cd hotel-service
    - mvn spring-boot:run

3) Booking Service:
    - cd booking-service
    - mvn spring-boot:run

4) Gateway:
    - cd api-gateway
    - mvn spring-boot:run

Gateway: http://localhost:8080
Eureka:  http://localhost:8761

Swagger:
- Booking: http://localhost:8082/swagger-ui.html
- Hotel:   http://localhost:8081/swagger-ui.html

## Quick test (manual)

1) Register user:
   POST http://localhost:8080/api/user/register
   {
   "username": "user1",
   "password": "pass"
   }

2) Use token, create booking:
   POST http://localhost:8080/api/booking
   Authorization: Bearer <token>
   {
   "requestId": "req-1",
   "autoSelect": false,
   "roomId": 1,
   "startDate": "2026-01-10",
   "endDate": "2026-01-12"
   }
