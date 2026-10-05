from pydantic import BaseModel
from datetime import datetime
from typing import Optional

class PaymentBase(BaseModel):
    order_id: int
    method: str
    amount: float

class PaymentConfirm(BaseModel):
    transaction_code: Optional[str] = None

class Payment(PaymentBase):
    id: int
    status: str
    transaction_code: Optional[str] = None
    created_at: datetime
    paid_at: Optional[datetime] = None

    class Config:
        from_attributes = True
