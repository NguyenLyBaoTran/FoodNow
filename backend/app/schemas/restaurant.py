from pydantic import BaseModel
from typing import List, Optional

class FoodBase(BaseModel):
    name: str
    description: Optional[str] = None
    price: float
    image_url: Optional[str] = None
    is_available: bool = True

class Food(FoodBase):
    id: int
    restaurant_id: int
    category_id: int

    class Config:
        from_attributes = True

class CategoryBase(BaseModel):
    name: str

class Category(CategoryBase):
    id: int
    restaurant_id: int
    foods: List[Food] = []

    class Config:
        from_attributes = True

class RestaurantBase(BaseModel):
    name: str
    address: str
    open_hours: Optional[str] = None
    logo_url: Optional[str] = None
    cover_url: Optional[str] = None

class Restaurant(RestaurantBase):
    id: int

    class Config:
        from_attributes = True

class RestaurantDetail(Restaurant):
    categories: List[Category] = []
    foods: List[Food] = []
