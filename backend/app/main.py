from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from .core.config import settings
from .core.database import init_db
from .routers import auth, restaurants, cart, orders, favorites, reviews, addresses, payments, users, driver, admin

app = FastAPI(title=settings.PROJECT_NAME)

@app.on_event("startup")
def startup_event():
    init_db()

app.include_router(auth.router, prefix="/api")
app.include_router(restaurants.router, prefix="/api")
app.include_router(cart.router, prefix="/api")
app.include_router(orders.router, prefix="/api")
app.include_router(favorites.router, prefix="/api")
app.include_router(reviews.router, prefix="/api")
app.include_router(addresses.router, prefix="/api")
app.include_router(payments.router, prefix="/api")
app.include_router(users.router, prefix="/api")
app.include_router(driver.router, prefix="/api")
app.include_router(admin.router, prefix="/api")

# Mount static files for Admin Web Panel
app.mount("/admin", StaticFiles(directory="app/static/admin", html=True), name="admin")


@app.get("/")
def read_root():
    return {"message": "Welcome to FoodNow API", "status": "healthy"}

@app.get("/health")
def health_check():
    return {"status": "up"}
