# FoodNow

FoodNow is a food ordering system developed with an **Android application, FastAPI REST API, MySQL database, and Admin Web**.

The system supports three main roles: **Customer, Driver, and Admin**.

---

## Table of Contents

- [System Overview](#system-overview)
- [Technology Stack](#technology-stack)
- [How to Run](#how-to-run)
- [Android Configuration](#android-configuration)
- [Demo Accounts](#demo-accounts)
- [Demo Flow](#demo-flow)
- [Quick Start](#quick-start)

---

## System Overview

| Component | Description |
|---|---|
| Customer App | Browse restaurants, order food, and track orders |
| Driver App | Receive orders and manage deliveries |
| Admin Web | Manage system data and monitor orders |
| Backend | Provides REST APIs using FastAPI |
| Database | Stores application data using MySQL |

### Architecture

```text
Customer / Driver Android App
              |
           Retrofit
              |
              v
        FastAPI Backend
              |
          SQLAlchemy
              |
              v
         MySQL Database


Admin Web  ──────>  FastAPI Backend
```

---

## Technology Stack

| Component | Technology |
|---|---|
| Android | Java + XML |
| Networking | Retrofit + OkHttp |
| Backend | FastAPI |
| ORM | SQLAlchemy |
| Database | MySQL 8.0 |
| Authentication | JWT |
| Admin Web | HTML + CSS + JavaScript |
| Gradle JVM | JDK 17 |

---

# How to Run

## 1. Start MySQL

Make sure **MySQL 8.0** is running and the `foodnow` database is available.

Backend configuration is stored in:

```text
backend/.env
```

Example database configuration:

```env
DATABASE_URL=mysql+pymysql://root:YOUR_PASSWORD@127.0.0.1:3306/foodnow?charset=utf8mb4
```

Replace `YOUR_PASSWORD` with your local MySQL password.

---

## 2. Start the Backend

Open a terminal from the FoodNow project directory:

```powershell
cd backend
```

Activate the virtual environment:

```powershell
.\.venv\Scripts\Activate.ps1
```

Install dependencies if needed:

```powershell
pip install -r requirements.txt
```

Start FastAPI:

```powershell
python -m uvicorn app.main:app --host 0.0.0.0 --port 8002
```

### Backend URLs

| Service | URL |
|---|---|
| Backend | `http://127.0.0.1:8002` |
| Swagger | `http://127.0.0.1:8002/docs` |
| Health Check | `http://127.0.0.1:8002/health` |
| Admin Web | `http://127.0.0.1:8002/admin/` |

Keep the backend terminal running while using the Android application.

---

# Android Configuration

Open the FoodNow project in **Android Studio** and wait for Gradle Sync to finish.

The project uses:

```text
Gradle JVM: JDK 17
Android Java Compatibility: Java 11
```

## Android Emulator

Set the Retrofit `BASE_URL` to:

```java
private static final String BASE_URL =
        "http://10.0.2.2:8002/api/";
```

Then select an emulator and run the `app` configuration.

> `10.0.2.2` allows the Android Emulator to access the backend running on the host computer.

---

## Physical Android Device

The Android phone and laptop must be connected to the **same Wi-Fi network**.

### Step 1 — Find the laptop IPv4 address

Run:

```powershell
ipconfig
```

Find the IPv4 address of the active Wi-Fi adapter.

Example:

```text
192.168.1.48
```

### Step 2 — Configure Retrofit

Replace the `BASE_URL` with the laptop IPv4 address:

```java
private static final String BASE_URL =
        "http://192.168.1.48:8002/api/";
```

The IP address above is only an example. Use the current IPv4 address of the laptop.

### Step 3 — Check the connection

On the phone browser, open:

```text
http://<LAPTOP_IPV4>:8002/health
```

If the health endpoint works, run FoodNow from Android Studio.

> A physical Android device must use the laptop IPv4 address instead of `10.0.2.2`.

---

# Demo Accounts

| Role | Email | Password |
|---|---|---|
| Admin | `admin@foodnow.com` | `admin123` |
| Customer | `customer@example.com` | `password123` |
| Driver 1 | `driver1@foodnow.com` | `password123` |
| Driver 2 | `driver2@foodnow.com` | `password123` |

---

# Demo Flow

## Customer

```text
Login
  ↓
Home
  ↓
Restaurant & Menu
  ↓
Cart
  ↓
Checkout
  ↓
Place Order
  ↓
Order History
  ↓
Order Tracking
```

## Driver

```text
Login
  ↓
Available Orders
  ↓
Accept Order
  ↓
Active Delivery
  ↓
Update Delivery Status
  ↓
Complete Delivery
```

## Admin

Open:

```text
http://127.0.0.1:8002/admin/
```

Then follow:

```text
Login
  ↓
Dashboard
  ↓
Orders / Users / Drivers
  ↓
Restaurants / Foods
```

---

# Quick Start

The recommended startup order is:

```text
1. Start MySQL
        ↓
2. Check backend/.env
        ↓
3. Start FastAPI
        ↓
4. Check /health
        ↓
5. Configure Android BASE_URL
        ↓
6. Run Android App
        ↓
7. Login using a demo account
```

### Backend

```powershell
cd backend
.\.venv\Scripts\Activate.ps1
python -m uvicorn app.main:app --host 0.0.0.0 --port 8002
```

### Android Build

From the FoodNow root directory:

```powershell
.\gradlew assembleDebug
```

Expected result:

```text
BUILD SUCCESSFUL
```

---

## Notes

- Keep MySQL and FastAPI running while using FoodNow.
- Use `10.0.2.2` when running with an Android Emulator.
- Use the laptop IPv4 address when running on a physical Android device.
- The physical phone and laptop must be on the same Wi-Fi network.
- Electronic payment functions are used for demonstration purposes.