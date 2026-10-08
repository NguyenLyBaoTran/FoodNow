# FoodNow

FoodNow is a food ordering and delivery demo built with **Android (Java/XML)**, **FastAPI (Python)**, **MySQL 8.0**, and an **Admin Web** interface. It supports three roles: **Customer, Driver, and Admin**.

## Table of Contents

- [System Overview](#system-overview)
- [Technology Stack](#technology-stack)
- [Requirements](#requirements)
- [Installation and Setup](#installation-and-setup)
- [Android Configuration](#android-configuration)
- [Demo Accounts](#demo-accounts)
- [Demo Flow](#demo-flow)
- [Quick Start](#quick-start)
- [Notes](#notes)

## System Overview

| Component | Description |
|---|---|
| Customer Android App | Browse restaurants, order food, and track orders |
| Driver Android App | Accept orders and update delivery status |
| Admin Web | View and manage system data |
| FastAPI Backend | REST APIs, authentication, and business logic |
| MySQL Database | Stores users, restaurants, foods, orders, and payments |

### Architecture

```text
Customer / Driver Android App
          |
    Retrofit (JSON)
          |
          v
     FastAPI Backend <----- Admin Web (HTML/CSS/JavaScript)
          |
      SQLAlchemy
          |
          v
      MySQL 8.0
```

The Admin Web is served by FastAPI. **PHP, phpMyAdmin, XAMPP, and Apache are not required.**

## Technology Stack

| Component | Technology |
|---|---|
| Android | Java + XML |
| Networking | Retrofit + OkHttp |
| Backend | Python + FastAPI |
| ORM | SQLAlchemy |
| Database | MySQL 8.0 |
| Authentication | JWT |
| Admin Web | HTML + CSS + JavaScript |
| Gradle JVM | JDK 17 |
| Android Java compatibility | Java 11 |

## Requirements

For a new **Windows** computer, install:

1. **Git** — to clone the repository (or download the GitHub ZIP).
2. **Python 3.13** — to run the FastAPI backend.
3. **MySQL Server 8.0** — to store application data.
4. **Android Studio + JDK 17** — to build and run the Android application.
5. **MySQL Workbench** (optional) — to inspect and manage the database.

## Installation and Setup

### 1. Download the project

```powershell
git clone https://github.com/NguyenLyBaoTran/FoodNow.git
cd FoodNow
```

Alternatively, use **GitHub → Code → Download ZIP**, extract it, and open the `FoodNow` folder. Private repositories require GitHub access permission.

### 2. Prepare MySQL

Start **MySQL Server 8.0** and create the database:

```sql
CREATE DATABASE IF NOT EXISTS foodnow
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

You can run this SQL in MySQL Workbench or the MySQL command-line client. Use the MySQL username and password configured on your computer.

### 3. Configure the backend environment

Open PowerShell in the project root:

```powershell
cd backend
Copy-Item .env.example .env
```

Open `backend/.env` and set the required values using `backend/.env.example` as the reference. For example, if the backend uses this database URL format:

```env
DATABASE_URL=mysql+pymysql://root:YOUR_PASSWORD@127.0.0.1:3306/foodnow?charset=utf8mb4
```

Replace `YOUR_PASSWORD` with your MySQL password. Make sure the actual configuration keys and database driver match the project's `backend/.env.example` and `backend/requirements.txt`.

**Do not commit or share `backend/.env`.**

### 4. Install Python dependencies

In the `backend` directory:

```powershell
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
```

This creates a new virtual environment on the computer; no existing `.venv` is needed.

### 5. Initialize and seed the database

The repository includes:

```text
backend/init_db.py
backend/seed/seed_data.py
```

Run the database initialization script and then the seed script **using the invocation supported by their imports and instructions**. These scripts must be executed before demo accounts and sample restaurants/orders can be expected to exist. If necessary, inspect `backend/README.md` and the scripts to confirm their exact commands.

**Do not run `backend/reset_db.py` on a database containing data you want to keep.**

### 6. Start FastAPI

Still in `backend`:

```powershell
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8002
```

| Service | URL |
|---|---|
| Health Check | http://127.0.0.1:8002/health |
| Swagger API Docs | http://127.0.0.1:8002/docs |
| Admin Web | http://127.0.0.1:8002/admin/ |

Keep MySQL and the backend running while using FoodNow.

## Android Configuration

Open the **FoodNow project root** in Android Studio and wait for Gradle Sync. Use **JDK 17** as the Gradle JVM; the Android Java source/target compatibility is **Java 11**.

Find the Retrofit base URL in:

```text
app/src/main/java/com/example/foodnow/network/RetrofitClient.java
```

### Android Emulator

Use:

```java
private static final String BASE_URL = "http://10.0.2.2:8002/api/";
```

`10.0.2.2` lets the Android Emulator reach the backend on the computer.

### Physical Android Device

1. Connect the Android phone and computer to the **same Wi-Fi network**.
2. On the computer, run `ipconfig` and find the active network adapter's **IPv4 Address**.
3. Update `BASE_URL` using that address. For example:

```java
private static final String BASE_URL = "http://192.168.1.48:8002/api/";
```

The IP above is **only an example**. It must be replaced with the current computer's IPv4 address.

4. Open `http://<LAPTOP_IPV4>:8002/health` in the phone browser to check connectivity.
5. Run the Android `app` configuration from Android Studio.

If the phone cannot reach `/health`, check that FastAPI is running with `--host 0.0.0.0`, both devices are on the same network, and Windows Firewall allows the connection.

### Build APK

From the project root:

```powershell
.\gradlew assembleDebug
```

Expected output: `BUILD SUCCESSFUL`.

## Demo Accounts

These accounts are available **after the demo seed data has been created successfully**.

| Role | Email | Password |
|---|---|---|
| Admin | `admin@foodnow.com` | `admin123` |
| Customer | `customer@example.com` | `password123` |
| Driver 1 | `driver1@foodnow.com` | `password123` |
| Driver 2 | `driver2@foodnow.com` | `password123` |

## Demo Flow

**Customer**

```text
Login → Home → Restaurant & Menu → Cart → Checkout
→ Place Order → Order History → Order Tracking
```

**Driver**

```text
Login → Available Orders → Accept Order
→ Active Delivery → Update Status → Complete Delivery
```

**Admin**

```text
Open http://127.0.0.1:8002/admin/
→ Login → Dashboard → Orders / Users / Drivers / Restaurants / Foods
```

## Quick Start

Once the project has been installed, configured, and seeded:

```text
1. Start MySQL
2. Start FastAPI (port 8002)
3. Check http://127.0.0.1:8002/health
4. Open the Admin Web or configure Android BASE_URL
5. Run the Android app
6. Sign in with a demo account
```

Backend command (from `backend`):

```powershell
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8002
```

## Notes

- This is a **local educational demo**, not a publicly deployed service.
- The Android app requires the FastAPI backend and MySQL database to be running.
- For a physical phone, the computer and phone must be on the same Wi-Fi network.
- Electronic payment functions are simulated for demonstration purposes.
- The `.env` file and local virtual environment are intentionally excluded from GitHub.
