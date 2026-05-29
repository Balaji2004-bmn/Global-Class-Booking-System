# Global Class Offering Booking System

## Project Overview

This project is a backend service for managing global live-learning class offerings and bookings.

Teachers can:

* Create offerings
* Add sessions
* View offerings

Parents/students can:

* View offerings in their local timezone
* Book offerings
* View bookings

The system prevents overlapping bookings and safely handles concurrent booking requests.

---

# Tech Stack

* Java 21
* Spring Boot 3
* PostgreSQL
* Spring Data JPA
* Hibernate
* Docker
* Maven

---

# Architecture

The application follows layered architecture:

Controller Layer → Service Layer → Repository Layer → PostgreSQL

---

# Features

* Course offering management
* Session scheduling
* Timezone-aware scheduling
* Booking conflict detection
* Concurrent booking safety
* Global exception handling
* REST APIs
* Dockerized PostgreSQL setup

---

# Database Design

Tables:

* teachers
* parents
* courses
* offerings
* sessions
* bookings

---

# Timezone Handling

* Teachers create sessions in their local timezone
* Backend converts and stores all timestamps in UTC
* Parent-facing APIs convert UTC timestamps into parent timezone dynamically

Why UTC?

* Avoids timezone inconsistencies
* Handles daylight saving safely
* Production best practice

---

# Concurrency Handling

Booking operations use:

* @Transactional
* Pessimistic locking

This prevents:

* race conditions
* duplicate bookings
* overlapping bookings during simultaneous requests

---

# Booking Conflict Logic

Two sessions overlap if:

existing.start < new.end
AND
existing.end > new.start

If overlap exists:

* booking is rejected with HTTP 409 Conflict

---

# Setup Instructions

## 1. Clone Repository

git clone https://github.com/Balaji2004-bmn/Global-Class-Booking-System.git

## 2. Start PostgreSQL

docker compose up -d

## 3. Run Application

mvn spring-boot:run

Application runs at:
http://localhost:8080

---

# API Documentation

Swagger:
http://localhost:8080/swagger-ui/index.html

OR

Use Postman collection:
postman/Global-Class-Offering-Booking-System.postman_collection.json

---

# Running Tests

mvn test

---

# Assumptions

* Booking occurs at offering level
* Sessions are immutable after booking
* All timestamps stored in UTC

---

# Future Improvements

* Authentication & Authorization
* Redis caching
* Notification service
* Deployment pipeline
* Rate limiting
* Monitoring & logging
