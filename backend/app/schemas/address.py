from pydantic import BaseModel, Field


class AddressCreate(BaseModel):
    recipient_name: str = Field(..., min_length=1)
    phone: str = Field(..., min_length=1)
    address_line: str = Field(..., min_length=1)
    is_default: bool = False


class AddressUpdate(BaseModel):
    recipient_name: str = Field(..., min_length=1)
    phone: str = Field(..., min_length=1)
    address_line: str = Field(..., min_length=1)
    is_default: bool = False


class AddressRead(AddressCreate):
    id: int
    user_id: int

    class Config:
        from_attributes = True
