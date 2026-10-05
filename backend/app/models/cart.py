from sqlalchemy import Column, Integer, ForeignKey
from sqlalchemy.orm import relationship
from ..core.database import Base

class Cart(Base):
    __tablename__ = "carts"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), unique=True)

    items = relationship("CartItem", back_populates="cart", cascade="all, delete-orphan")

class CartItem(Base):
    __tablename__ = "cart_items"

    id = Column(Integer, primary_key=True, index=True)
    cart_id = Column(Integer, ForeignKey("carts.id"))
    food_id = Column(Integer, ForeignKey("foods.id"))
    quantity = Column(Integer, default=1)

    cart = relationship("Cart", back_populates="items")
    food = relationship("Food")
