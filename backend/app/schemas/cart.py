from pydantic import BaseModel
from typing import List, Optional
from .restaurant import Food

class CartItemBase(BaseModel):
    food_id: int
    quantity: int = 1

class CartItemCreate(CartItemBase):
    pass

class CartItemUpdate(BaseModel):
    quantity: int

class CartItem(CartItemBase):
    id: int
    cart_id: int
    food: Food

    class Config:
        from_attributes = True

class Cart(BaseModel):
    id: int
    user_id: int
    items: List[CartItem] = []

    class Config:
        from_attributes = True
