from typing import List

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from ..core.database import get_db
from ..core.deps import get_current_user
from ..models import user as user_models
from ..models.address import Address
from ..schemas.address import AddressCreate, AddressRead, AddressUpdate

router = APIRouter(prefix="/addresses", tags=["addresses"])


@router.get("/", response_model=List[AddressRead])
def get_addresses(
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user),
):
    return (
        db.query(Address)
        .filter(Address.user_id == current_user.id)
        .order_by(Address.id.desc())
        .all()
    )


@router.post("/", response_model=AddressRead, status_code=status.HTTP_201_CREATED)
def create_address(
    address_in: AddressCreate,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user),
):
    if address_in.is_default:
        db.query(Address).filter(
            Address.user_id == current_user.id,
            Address.is_default.is_(True),
        ).update({"is_default": False})

    db_address = Address(
        user_id=current_user.id,
        recipient_name=address_in.recipient_name,
        phone=address_in.phone,
        address_line=address_in.address_line,
        is_default=address_in.is_default,
    )
    db.add(db_address)
    db.commit()
    db.refresh(db_address)
    return db_address


@router.put("/{address_id}/", response_model=AddressRead)
def update_address(
    address_id: int,
    address_in: AddressUpdate,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user),
):
    db_address = db.query(Address).filter(Address.id == address_id).first()
    if not db_address:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Address not found")
    if db_address.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You do not have permission to update this address",
        )

    if address_in.is_default:
        db.query(Address).filter(
            Address.user_id == current_user.id,
            Address.is_default.is_(True),
            Address.id != address_id,
        ).update({"is_default": False})

    db_address.recipient_name = address_in.recipient_name
    db_address.phone = address_in.phone
    db_address.address_line = address_in.address_line
    db_address.is_default = address_in.is_default

    db.commit()
    db.refresh(db_address)
    return db_address


@router.delete("/{address_id}/", status_code=status.HTTP_204_NO_CONTENT)
def delete_address(
    address_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user),
):
    db_address = db.query(Address).filter(Address.id == address_id).first()
    if not db_address:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Address not found")
    if db_address.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You do not have permission to delete this address",
        )

    db.delete(db_address)
    db.commit()
    return None
