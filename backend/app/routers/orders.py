from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from ..core.database import get_db
from ..core.deps import get_current_user
from ..models import user as user_models
from ..models import order as models
from ..models import cart as cart_models
from ..models import food as food_models
from ..models import payment as payment_models
from ..schemas import order as schemas

router = APIRouter(prefix="/orders", tags=["orders"])

@router.post("/", response_model=schemas.Order)
def create_order(
    order_in: schemas.OrderCreate,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    # 1. Get Cart
    cart = db.query(cart_models.Cart).filter(cart_models.Cart.user_id == current_user.id).first()
    if not cart or not cart.items:
        raise HTTPException(status_code=400, detail="Cart is empty")

    # 2. Calculate Total and prepare items
    total_amount = 0
    order_items = []

    for item in cart.items:
        food = db.query(food_models.Food).filter(food_models.Food.id == item.food_id).first()
        if not food:
            continue

        price_snapshot = food.price
        total_amount += price_snapshot * item.quantity

        order_item = models.OrderItem(
            food_id=item.food_id,
            quantity=item.quantity,
            price_snapshot=price_snapshot
        )
        order_items.append(order_item)

    # 3. Create Order
    # Determine initial status based on payment method
    initial_status = models.OrderStatus.PENDING_PAYMENT
    if order_in.payment_method == payment_models.PaymentMethod.COD:
        initial_status = models.OrderStatus.WAITING_FOR_DRIVER

    db_order = models.Order(
        user_id=current_user.id,
        address_id=order_in.address_id,
        total_amount=total_amount,
        payment_method=order_in.payment_method,
        status=initial_status
    )
    db.add(db_order)
    db.commit()
    db.refresh(db_order)

    # 4. Save Items
    for item in order_items:
        item.order_id = db_order.id
        db.add(item)

    # 5. Create Payment record
    db_payment = payment_models.Payment(
        order_id=db_order.id,
        method=order_in.payment_method,
        amount=total_amount,
        status=payment_models.PaymentStatus.PENDING
    )
    # If COD, payment is PENDING but order is WAITING_FOR_DRIVER
    db.add(db_payment)

    # 6. Clear Cart
    db.query(cart_models.CartItem).filter(cart_models.CartItem.cart_id == cart.id).delete()

    db.commit()
    db.refresh(db_order)
    return db_order

@router.get("/", response_model=List[schemas.Order])
def get_orders(db: Session = Depends(get_db), current_user: user_models.User = Depends(get_current_user)):
    return db.query(models.Order).filter(models.Order.user_id == current_user.id).order_by(models.Order.created_at.desc()).all()

@router.get("/{order_id}/", response_model=schemas.Order)
def get_order(
    order_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    db_order = db.query(models.Order).filter(models.Order.id == order_id).first()
    if not db_order:
        raise HTTPException(status_code=404, detail="Order not found")
    if db_order.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized to access this order")
    return db_order

@router.post("/{order_id}/cancel/", response_model=schemas.Order)
def cancel_order(
    order_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    db_order = db.query(models.Order).filter(models.Order.id == order_id).first()
    if not db_order:
        raise HTTPException(status_code=404, detail="Order not found")

    if db_order.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized to cancel this order")

    allowed_statuses = [models.OrderStatus.PENDING_PAYMENT, models.OrderStatus.WAITING_FOR_DRIVER]
    if db_order.status not in allowed_statuses:
        raise HTTPException(status_code=400, detail=f"Cannot cancel order in status: {db_order.status}")

    db_order.status = models.OrderStatus.CANCELLED

    # If there is a payment, mark it as CANCELLED too
    if db_order.payment:
        db_order.payment.status = payment_models.PaymentStatus.CANCELLED

    db.commit()
    db.refresh(db_order)
    return db_order

@router.put("/{order_id}/items/{item_id}/", response_model=schemas.Order)
def update_order_item(
    order_id: int,
    item_id: int,
    item_in: schemas.OrderItemUpdate,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    db_order = db.query(models.Order).filter(models.Order.id == order_id).first()
    if not db_order:
        raise HTTPException(status_code=404, detail="Order not found")
    if db_order.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")

    if db_order.status not in [models.OrderStatus.PENDING_PAYMENT, models.OrderStatus.WAITING_FOR_DRIVER]:
        raise HTTPException(status_code=400, detail="Order is not editable")

    db_item = db.query(models.OrderItem).filter(models.OrderItem.id == item_id, models.OrderItem.order_id == order_id).first()
    if not db_item:
        raise HTTPException(status_code=404, detail="Item not found")

    if item_in.quantity < 1:
        raise HTTPException(status_code=400, detail="Quantity must be at least 1")

    db_item.quantity = item_in.quantity

    # Recalculate total
    total = 0
    db.flush() # Ensure quantity update is reflected
    for item in db_order.items:
        total += item.price_snapshot * item.quantity
    db_order.total_amount = total

    if db_order.payment:
        db_order.payment.amount = total

    db.commit()
    db.refresh(db_order)
    return db_order

@router.delete("/{order_id}/items/{item_id}/", response_model=schemas.Order)
def delete_order_item(
    order_id: int,
    item_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    db_order = db.query(models.Order).filter(models.Order.id == order_id).first()
    if not db_order:
        raise HTTPException(status_code=404, detail="Order not found")
    if db_order.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")

    if db_order.status not in [models.OrderStatus.PENDING_PAYMENT, models.OrderStatus.WAITING_FOR_DRIVER]:
        raise HTTPException(status_code=400, detail="Order is not editable")

    db_item = db.query(models.OrderItem).filter(models.OrderItem.id == item_id, models.OrderItem.order_id == order_id).first()
    if not db_item:
        raise HTTPException(status_code=404, detail="Item not found")

    if len(db_order.items) <= 1:
        raise HTTPException(status_code=400, detail="Cannot delete the last item. Cancel the order instead.")

    db.delete(db_item)
    db.commit()
    db.refresh(db_order)

    # Recalculate total
    total = 0
    for item in db_order.items:
        total += item.price_snapshot * item.quantity
    db_order.total_amount = total

    if db_order.payment:
        db_order.payment.amount = total

    db.commit()
    db.refresh(db_order)
    return db_order
