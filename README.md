# E-Wallet
# 💸 Mini E-Wallet Core System

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen)

> A high-performance, transactional e-wallet backend system designed to demonstrate **Banking-level Data Integrity**, **Concurrency Handling**, and **Secure Payment Integration**.

## 📖 Introduction

This project is not just a CRUD application. It is a simulation of a core banking engine focusing on the accuracy of financial transactions. It solves critical fintech challenges such as **Race Conditions**, **Deadlocks**, **Double-Spending**, and **Idempotency** using advanced Spring Boot techniques.

## 🚀 Key Features & Technical Highlights

Here are the engineering problems solved in this project:

### 1. 🛡️ Concurrency & Deadlock Prevention
* **Problem:** When User A sends money to User B, and User B sends money to User A simultaneously, a Database Deadlock often occurs.
* **Solution:** Implemented a **Lock Ordering Algorithm** (always lock the account with the smaller ID first) combined with `Pessimistic Locking` to eliminate deadlocks and ensure atomic transactions.

### 2. 💰 Financial Data Precision
* **Problem:** Using `Double` or `Float` causes rounding errors in financial calculations.
* **Solution:** strictly used `BigDecimal` for all monetary values.
* **Database:** Configured MySQL `DECIMAL(19, 4)` to support values up to quadrillions with 4 decimal places precision.

### 3. 🔄 Idempotency (Anti-Duplicate Processing)
* **Problem:** Payment gateways (like VNPay) might send the same Webhook (IPN) multiple times due to network latency, leading to double crediting.
* **Solution:** Implemented an **Idempotency Key** mechanism using Redis/Database to track processed transaction references (`provider_ref_id`). Duplicate requests are detected and ignored safely.

### 4. 🔒 Security & Optimization
* **Authentication:** Stateless authentication using **JWT (JSON Web Token)**.
* **Optimization:** Used **Lazy Loading** for Entity relationships to prevent N+1 query issues.
* **Validation:** Strict input validation (`@DecimalMin`, `@NotNull`) to prevent logical errors.

---

## 🛠️ Tech Stack

* **Core:** Java 17, Spring Boot 3.2.5
* **Database:** MySQL 8.0 (Persistence)
* **ORM:** Spring Data JPA (Hibernate)
* **Security:** Spring Security, JWT
* **Payment Gateway:** VNPay (Sandbox Environment)
* **Tools:** Maven, Lombok, Postman

---

## 🗄️ Database Schema

The database follows the **Double-Entry Bookkeeping** principle simplified for micro-wallets.

| Table | Description | Key Fields |
| :--- | :--- | :--- |
| **`users`** | Identity management | `email`, `password` (BCrypt), `role` |
| **`wallets`** | Asset management | `balance` (DECIMAL), `user_id`, `version` (Optimistic Lock) |
| **`transactions`** | Immutable Ledger | `id` (UUID), `amount`, `type` (DEPOSIT/TRANSFER), `status` |
| **`idempotency_logs`**| Webhook Logs | `key_id`, `response_json` |

---

## 📂 Project Structure (Layered Architecture)

```text
src/main/java/com/hcmuaf/ewallet
├── config/             # App configurations (Security, Redis, OpenAPI)
├── controller/         # REST API Layer
├── service/            # Business Logic (Transaction handling, Locking)
├── repository/         # DB Access Layer
├── entity/             # JPA Entities
├── dto/                # Data Transfer Objects
├── exception/          # Global Exception Handling
└── util/               # Helper classes (Security, Signature generation)
