# FoodNow Backend

## Local MySQL setup

The backend uses SQLAlchemy with MySQL through PyMySQL. MySQL must be running on `127.0.0.1:3306`.

1. Create the database using a MySQL account with permission to create databases:

```sql
CREATE DATABASE foodnow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Copy `.env.example` to `.env` and set the local MySQL password:

```env
DATABASE_URL=mysql+pymysql://root:YOUR_MYSQL_PASSWORD@127.0.0.1:3306/foodnow?charset=utf8mb4
```

Do not commit `.env` or put real credentials in `.env.example`.

3. Install backend dependencies and create the tables:

```powershell
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe init_db.py
```

4. Seed the demo data:

```powershell
.\.venv\Scripts\python.exe seed\seed_data.py
```

The seed reuses existing users without changing their passwords or profiles, ensures each seeded user has one cart, and creates demo records without resetting the database. The current dataset contains 7 restaurants, 8 categories, and 34 foods. The demo accounts are `admin@foodnow.com` / `admin123`, `customer@example.com` / `password123`, and `driver1@foodnow.com` plus `driver2@foodnow.com` / `password123` when those accounts are newly seeded. Existing account credentials and roles are preserved.

5. Start the API on the existing port:

```powershell
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8002
```

The seed does not reset or drop the database. Do not run `reset_db.py` for normal setup.
