from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime
from .order import Order
from .user import User

class DashboardStats(BaseModel):
    total_users: int
    total_drivers: int
    total_restaurants: int
    total_foods: int
    total_orders: int
    recent_orders: List[Order]

class DriverSummary(BaseModel):
    id: int
    full_name: Optional[str]
    email: str
    is_active: bool
    total_completed_orders: int
    current_order_id: Optional[int]

class DriverDetail(BaseModel):
    user: User
    total_orders: int
    completed_orders: int
    cancelled_orders: int
    active_order: Optional[Order]

class UserSummary(BaseModel):
    id: int
    full_name: Optional[str]
    email: str
    role: str
    is_active: bool
    created_at: datetime

class UserDetail(BaseModel):
    user: User
    total_orders: int
    total_reviews: int
    total_addresses: int

class RestaurantSummary(BaseModel):
    id: int
    name: str
    address: str
    open_hours: Optional[str]
    logo_url: Optional[str]
    cover_url: Optional[str]
    food_count: int
    category_count: int
    can_delete: bool

class RestaurantDetailAdmin(BaseModel):
    id: int
    name: str
    address: str
    open_hours: Optional[str]
    logo_url: Optional[str]
    cover_url: Optional[str]
    created_at: datetime
    food_count: int
    category_count: int
    total_orders: int
    can_delete: bool

class FoodSummary(BaseModel):
    id: int
    name: str
    price: float
    is_available: bool
    restaurant_name: str
    category_name: str
    can_delete: bool

class FoodDetailAdmin(BaseModel):
    id: int
    name: str
    description: Optional[str]
    price: float
    image_url: Optional[str]
    is_available: bool
    restaurant_id: int
    category_id: int
    can_delete: bool

class CategoryAdmin(BaseModel):
    id: int
    name: str
    restaurant_id: int

    class Config:
        from_attributes = True
