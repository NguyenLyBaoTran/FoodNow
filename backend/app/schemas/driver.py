from pydantic import BaseModel
from .order import Order

class OrderStatusUpdate(BaseModel):
    status: str
