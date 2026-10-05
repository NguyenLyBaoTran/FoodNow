from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime
from .restaurant import Food
from .payment import Payment
from .address import AddressRead
from .user import User

class OrderItemBase(BaseModel):
    food_id: int
    quantity: int
    price_snapshot: float

class OrderItem(OrderItemBase):
    id: int
    food: Food

    class Config:
        from_attributes = True

class OrderItemUpdate(BaseModel):
    quantity: int

class OrderBase(BaseModel):
    address_id: int
    payment_method: str = "COD"

class OrderCreate(OrderBase):
    pass

class Order(OrderBase):
    id: int
    user_id: int
    driver_id: Optional[int] = None
    total_amount: float
    status: str
    created_at: datetime
    items: List[OrderItem] = []
    payment: Optional[Payment] = None
    address: Optional[AddressRead] = None
    user: Optional[User] = None

    class Config:
        from_attributes = True
