# Hotel Booking System (микросервисная архитектура)

Учебный проект микросервисной системы бронирования отелей на **Spring Boot / Spring Cloud**  
с **JWT-аутентификацией**, **Service Discovery**, **API Gateway** и управляемой согласованностью
между сервисами.

---

## Состав системы

Проект состоит из следующих микросервисов:

- **eureka-server** — сервис регистрации и обнаружения (Service Discovery)
- **api-gateway** — единая точка входа (Spring Cloud Gateway)
- **hotel-service** — управление отелями и номерами, проверка доступности
- **booking-service** — регистрация пользователей, JWT-аутентификация, бронирования

Все сервисы:
- самостоятельные Spring Boot приложения
- используют in-memory базу **H2**
- валидируют JWT как **Resource Server**

---

## Архитектура (кратко)

- Вход всех клиентских запросов осуществляется через **API Gateway**
- Gateway маршрутизирует запросы в нужные сервисы
- **Hotel Service** содержит *internal* эндпойнты, недоступные через Gateway
- **Booking Service** при создании бронирования:
   1. создаёт бронирование со статусом `PENDING`
   2. вызывает `hotel-service` для подтверждения доступности номера
   3. при успехе → `CONFIRMED`
   4. при ошибке или таймауте → `CANCELLED` + компенсация (`release`)
- Идемпотентность обеспечивается параметром `requestId`
- Защита от гонок данных — пессимистическая блокировка номера

---

## Запуск через Docker (рекомендуется)

### Требования
- Docker
- Docker Compose (v2)

### Сборка и запуск всех сервисов
```docker compose up --build```

### Доступные адреса
Eureka - http://localhost:8761
API Gateway - http://localhost:8080
Swagger Hotel Service - http://localhost:8081/swagger-ui.html
Swagger Booking Service - http://localhost:8082/swagger-ui.html

### Роли и доступ

USER
- просмотр отелей и номеров
- создание бронирований
- просмотр своей истории бронирований
- отмена своих бронирований

ADMIN
- CRUD пользователей
- CRUD отелей
- CRUD номеров
- просмотр статистики

JWT и безопасность

Тип токена: HS256
Время жизни: 1 час
JWT содержит claim roles: ["USER"] или ["ADMIN"]
Каждый сервис валидирует JWT самостоятельно
Internal эндпойнты Hotel Service не публикуются через Gateway

### Запуск без Docker (локально)
Порядок запуска сервисов
1) Eureka Server
2) Hotel Service
3) Booking Service
4) API Gateway

```
mvn -pl eureka-server spring-boot:run
mvn -pl hotel-service spring-boot:run
mvn -pl booking-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

Тестирование

В проекте реализованы тесты:
Hotel Service 
- проверка пересечения дат
- идемпотентность confirm / release

Booking Service
- успешное бронирование
- таймаут и компенсация
- повторная доставка запроса (requestId)
- интеграционные тесты с WireMock