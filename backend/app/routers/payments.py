from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from datetime import datetime
from ..core.database import get_db
from ..core.deps import get_current_user
from ..models import order as order_models
from ..models import payment as payment_models
from ..models import user as user_models
from ..schemas import payment as schemas

router = APIRouter(prefix="/payments", tags=["payments"])

@router.post("/{payment_id}/confirm/", response_model=schemas.Payment)
def confirm_payment(
    payment_id: int,
    confirm_in: schemas.PaymentConfirm,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    db_payment = db.query(payment_models.Payment).filter(payment_models.Payment.id == payment_id).first()
    if not db_payment:
        raise HTTPException(status_code=404, detail="Payment not found")

    db_order = db.query(order_models.Order).filter(order_models.Order.id == db_payment.order_id).first()
    if not db_order or db_order.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized to access this payment")

    if db_payment.status == payment_models.PaymentStatus.PAID:
        return db_payment

    db_payment.status = payment_models.PaymentStatus.PAID
    db_payment.paid_at = datetime.utcnow()
    db_payment.transaction_code = confirm_in.transaction_code or f"SIM-{int(datetime.utcnow().timestamp())}"

    # Update Order status
    db_order.status = order_models.OrderStatus.WAITING_FOR_DRIVER

    db.commit()
    db.refresh(db_payment)
    return db_payment

@router.get("/{payment_id}/", response_model=schemas.Payment)
def get_payment(
    payment_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    db_payment = db.query(payment_models.Payment).filter(payment_models.Payment.id == payment_id).first()
    if not db_payment:
        raise HTTPException(status_code=404, detail="Payment not found")

    db_order = db.query(order_models.Order).filter(order_models.Order.id == db_payment.order_id).first()
    if not db_order or db_order.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized to access this payment")

    return db_payment
