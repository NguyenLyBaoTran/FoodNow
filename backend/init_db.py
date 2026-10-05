from sqlalchemy import inspect

from app.core.database import Base, engine
from app.models import Address, Cart, CartItem, Category, Favorite, Food, Order, OrderItem, Restaurant, Review, User


def init_database():
    print("Ensuring SQLAlchemy models are imported...")
    print("Creating any missing tables only...")
    Base.metadata.create_all(bind=engine)

    inspector = inspect(engine)
    tables = inspector.get_table_names()
    print(f"Database tables present: {tables}")


if __name__ == "__main__":
    init_database()
