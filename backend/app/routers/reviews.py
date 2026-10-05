from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from ..core.database import get_db
from ..core.deps import get_current_user
from ..models import user as user_models
from ..models import review as models
from ..models import food as food_models
from ..models import order as order_models
from ..schemas import review as schemas

router = APIRouter(prefix="/reviews", tags=["reviews"])

@router.get("/{food_id}/", response_model=List[schemas.Review])
def get_reviews(food_id: int, db: Session = Depends(get_db)):
    reviews = (
        db.query(models.Review, user_models.User.full_name)
        .join(user_models.User, user_models.User.id == models.Review.user_id)
        .filter(models.Review.food_id == food_id)
        .order_by(models.Review.created_at.desc())
        .all()
    )
    return [dict(review.__dict__, user_name=user_name) for review, user_name in reviews]


@router.get("/{food_id}/eligibility", response_model=schemas.ReviewEligibility)
def get_review_eligibility(
    food_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user),
):
    existing_review = (
        db.query(models.Review)
        .filter(models.Review.user_id == current_user.id, models.Review.food_id == food_id)
        .first()
    )
    if existing_review:
        return {"can_review": False, "already_reviewed": True, "message": "You already reviewed this food."}

    has_completed_order = (
        db.query(order_models.OrderItem)
        .join(order_models.Order, order_models.Order.id == order_models.OrderItem.order_id)
        .filter(
            order_models.Order.user_id == current_user.id,
            order_models.Order.status == order_models.OrderStatus.COMPLETED.value,
            order_models.OrderItem.food_id == food_id,
        )
        .first()
        is not None
    )
    if not has_completed_order:
        return {
            "can_review": False,
            "already_reviewed": False,
            "message": "You can review this food after completing an order.",
        }
    return {"can_review": True, "already_reviewed": False}

@router.post("/", response_model=schemas.Review)
def create_review(
    review_in: schemas.ReviewCreate,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    food = db.query(food_models.Food).filter(food_models.Food.id == review_in.food_id).first()
    if not food:
        raise HTTPException(status_code=404, detail="Food not found")

    existing_review = (
        db.query(models.Review)
        .filter(models.Review.user_id == current_user.id, models.Review.food_id == review_in.food_id)
        .first()
    )
    if existing_review:
        raise HTTPException(status_code=400, detail="You already reviewed this food.")

    has_completed_order = (
        db.query(order_models.OrderItem)
        .join(order_models.Order, order_models.Order.id == order_models.OrderItem.order_id)
        .filter(
            order_models.Order.user_id == current_user.id,
            order_models.Order.status == order_models.OrderStatus.COMPLETED.value,
            order_models.OrderItem.food_id == review_in.food_id,
        )
        .first()
        is not None
    )
    if not has_completed_order:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="You can review this food after completing an order.",
        )

    db_review = models.Review(
        user_id=current_user.id,
        food_id=review_in.food_id,
        rating=review_in.rating,
        comment=review_in.comment
    )
    db.add(db_review)
    db.commit()
    db.refresh(db_review)
    return dict(db_review.__dict__, user_name=current_user.full_name)
