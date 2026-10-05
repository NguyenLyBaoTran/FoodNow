from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from ..core.database import get_db
from ..core.deps import get_current_user
from ..models import order as order_models
from ..models import user as user_models
from ..schemas import order as order_schemas
from ..schemas import driver as driver_schemas

router = APIRouter(prefix="/driver", tags=["driver"])

def verify_driver(current_user: user_models.User):
    if current_user.role != user_models.UserRole.DRIVER:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="The user does not have enough privileges",
        )

@router.get("/orders/available", response_model=List[order_schemas.Order])
def get_available_orders(
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_driver(current_user)
    return db.query(order_models.Order).filter(
        order_models.Order.status == order_models.OrderStatus.WAITING_FOR_DRIVER,
        order_models.Order.driver_id == None
    ).all()

@router.get("/orders/active", response_model=List[order_schemas.Order])
def get_active_orders(
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_driver(current_user)
    excluded_statuses = [
        order_models.OrderStatus.COMPLETED,
        order_models.OrderStatus.CANCELLED,
        order_models.OrderStatus.DELIVERED
    ]
    return db.query(order_models.Order).filter(
        order_models.Order.driver_id == current_user.id,
        order_models.Order.status.notin_(excluded_statuses)
    ).all()

@router.get("/orders/history", response_model=List[order_schemas.Order])
def get_order_history(
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_driver(current_user)
    history_statuses = [
        order_models.OrderStatus.COMPLETED,
        order_models.OrderStatus.CANCELLED,
        order_models.OrderStatus.DELIVERED
    ]
    return db.query(order_models.Order).filter(
        order_models.Order.driver_id == current_user.id,
        order_models.Order.status.in_(history_statuses)
    ).all()

@router.post("/orders/{order_id}/accept", response_model=order_schemas.Order)
def accept_order(
    order_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_driver(current_user)
    db_order = db.query(order_models.Order).filter(order_models.Order.id == order_id).first()
    if not db_order:
        raise HTTPException(status_code=404, detail="Order not found")

    if db_order.status != order_models.OrderStatus.WAITING_FOR_DRIVER or db_order.driver_id is not None:
        raise HTTPException(status_code=400, detail="Order is no longer available")

    db_order.driver_id = current_user.id
    db_order.status = order_models.OrderStatus.DRIVER_ASSIGNED
    db.commit()
    db.refresh(db_order)
    return db_order

@router.post("/orders/{order_id}/status", response_model=order_schemas.Order)
def update_order_status(
    order_id: int,
    status_in: driver_schemas.OrderStatusUpdate,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_driver(current_user)
    db_order = db.query(order_models.Order).filter(
        order_models.Order.id == order_id,
        order_models.Order.driver_id == current_user.id
    ).first()

    if not db_order:
        raise HTTPException(status_code=404, detail="Active order not found for this driver")

    new_status = status_in.status
    # Simple validation of state transitions
    valid_transitions = {
        order_models.OrderStatus.DRIVER_ASSIGNED: [order_models.OrderStatus.PREPARING],
        order_models.OrderStatus.PREPARING: [order_models.OrderStatus.READY_FOR_PICKUP],
        order_models.OrderStatus.READY_FOR_PICKUP: [order_models.OrderStatus.PICKED_UP],
        order_models.OrderStatus.PICKED_UP: [order_models.OrderStatus.DELIVERING],
        order_models.OrderStatus.DELIVERING: [order_models.OrderStatus.DELIVERED],
        order_models.OrderStatus.DELIVERED: [order_models.OrderStatus.COMPLETED],
    }

    if db_order.status not in valid_transitions or new_status not in valid_transitions[db_order.status]:
         raise HTTPException(status_code=400, detail=f"Invalid status transition from {db_order.status} to {new_status}")

    db_order.status = new_status
    db.commit()
    db.refresh(db_order)
    return db_order
