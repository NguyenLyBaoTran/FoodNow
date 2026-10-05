from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from ..core.database import get_db
from ..core.deps import get_current_user
from ..models import user as user_models
from ..models import cart as models
from ..models import food as food_models
from ..schemas import cart as schemas

router = APIRouter(prefix="/cart", tags=["cart"])

@router.get("/", response_model=schemas.Cart)
def get_cart(db: Session = Depends(get_db), current_user: user_models.User = Depends(get_current_user)):
    # Use filter and first() to check existence
    cart = db.query(models.Cart).filter(models.Cart.user_id == current_user.id).first()
    if not cart:
        # User doesn't have a cart, create one
        cart = models.Cart(user_id=current_user.id)
        db.add(cart)
        try:
            db.commit()
            db.refresh(cart)
        except Exception:
            # Handle race condition if cart was created between the check and insert
            db.rollback()
            cart = db.query(models.Cart).filter(models.Cart.user_id == current_user.id).first()
    return cart

@router.post("/items/", response_model=schemas.CartItem)
def add_to_cart(
    item_in: schemas.CartItemCreate,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    cart = db.query(models.Cart).filter(models.Cart.user_id == current_user.id).first()
    if not cart:
        cart = models.Cart(user_id=current_user.id)
        db.add(cart)
        db.commit()
        db.refresh(cart)

    # Check if food exists
    food = db.query(food_models.Food).filter(food_models.Food.id == item_in.food_id).first()
    if not food:
        raise HTTPException(status_code=404, detail="Food not found")

    # Check if already in cart
    cart_item = db.query(models.CartItem).filter(
        models.CartItem.cart_id == cart.id,
        models.CartItem.food_id == item_in.food_id
    ).first()

    if cart_item:
        cart_item.quantity += item_in.quantity
    else:
        cart_item = models.CartItem(cart_id=cart.id, food_id=item_in.food_id, quantity=item_in.quantity)
        db.add(cart_item)

    db.commit()
    db.refresh(cart_item)
    return cart_item

@router.put("/items/{item_id}/", response_model=schemas.CartItem)
@router.patch("/items/{item_id}/", response_model=schemas.CartItem)
def update_cart_item(
    item_id: int,
    item_in: schemas.CartItemUpdate,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    if item_in.quantity < 1:
        raise HTTPException(status_code=400, detail="Quantity must be at least 1")

    cart_item = db.query(models.CartItem).filter(models.CartItem.id == item_id).first()
    if not cart_item:
        raise HTTPException(status_code=404, detail="Cart item not found")

    cart = db.query(models.Cart).filter(models.Cart.id == cart_item.cart_id).first()
    if not cart or cart.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="You do not have permission to modify this cart item")

    cart_item.quantity = item_in.quantity
    db.commit()
    db.refresh(cart_item)
    return cart_item

@router.delete("/items/{item_id}/", status_code=status.HTTP_204_NO_CONTENT)
def delete_cart_item(
    item_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    cart_item = db.query(models.CartItem).filter(models.CartItem.id == item_id).first()
    if not cart_item:
        raise HTTPException(status_code=404, detail="Cart item not found")

    cart = db.query(models.Cart).filter(models.Cart.id == cart_item.cart_id).first()
    if not cart or cart.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="You do not have permission to delete this cart item")

    db.delete(cart_item)
    db.commit()
    return None
