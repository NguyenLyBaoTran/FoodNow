from pydantic import BaseModel
from .restaurant import Food

class FavoriteBase(BaseModel):
    food_id: int

class Favorite(FavoriteBase):
    id: int
    user_id: int
    food: Food

    class Config:
        from_attributes = True