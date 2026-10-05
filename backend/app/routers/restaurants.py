from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from ..core.database import get_db
from ..models import restaurant as models
from ..schemas import restaurant as schemas

router = APIRouter(prefix="/restaurants", tags=["restaurants"])

@router.get("/", response_model=List[schemas.Restaurant])
def get_restaurants(db: Session = Depends(get_db)):
    return db.query(models.Restaurant).all()

@router.get("/{restaurant_id}/", response_model=schemas.RestaurantDetail)
def get_restaurant(restaurant_id: int, db: Session = Depends(get_db)):
    db_restaurant = db.query(models.Restaurant).filter(models.Restaurant.id == restaurant_id).first()
    if not db_restaurant:
        raise HTTPException(status_code=404, detail="Restaurant not found")
    return db_restaurant
