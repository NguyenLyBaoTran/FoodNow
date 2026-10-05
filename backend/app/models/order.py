from sqlalchemy import Column, Integer, String, ForeignKey, Float, DateTime, Enum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
import enum
from ..core.database import Base

class OrderStatus(str, enum.Enum):
    PENDING_PAYMENT = "PENDING_PAYMENT"
    WAITING_FOR_DRIVER = "WAITING_FOR_DRIVER"
    DRIVER_ASSIGNED = "DRIVER_ASSIGNED"
    PREPARING = "PREPARING"
    READY_FOR_PICKUP = "READY_FOR_PICKUP"
    PICKED_UP = "PICKED_UP"
    DELIVERING = "DELIVERING"
    DELIVERED = "DELIVERED"
    COMPLETED = "COMPLETED"
    CANCELLED = "CANCELLED"

class Order(Base):
    __tablename__ = "orders"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    driver_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    address_id = Column(Integer, ForeignKey("addresses.id"))
    total_amount = Column(Float, nullable=False)
    status = Column(String(30), default=OrderStatus.PENDING_PAYMENT)
    payment_method = Column(String(20)) # COD, MOMO, VNPAY
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", foreign_keys=[user_id])
    driver = relationship("User", foreign_keys=[driver_id])
    address = relationship("Address")
    items = relationship("OrderItem", back_populates="order")
    payment = relationship("Payment", back_populates="order", uselist=False)

class OrderItem(Base):
    __tablename__ = "order_items"

    id = Column(Integer, primary_key=True, index=True)
    order_id = Column(Integer, ForeignKey("orders.id"))
    food_id = Column(Integer, ForeignKey("foods.id"))
    quantity = Column(Integer, nullable=False)
    price_snapshot = Column(Float, nullable=False) # Price at time of order

    order = relationship("Order", back_populates="items")
    food = relationship("Food")
