import os
import sys

from app.core.database import Base, engine
from app.models import Address, Cart, CartItem, Category, Favorite, Food, Order, OrderItem, Restaurant, Review, User


def reset_database():
    print("Dropping all tables...")
    Base.metadata.drop_all(bind=engine)
    print("Creating all tables...")
    Base.metadata.create_all(bind=engine)
    print("Database reset successfully.")


if __name__ == "__main__":
    reset_database()
