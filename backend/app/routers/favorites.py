from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from ..core.database import get_db
from ..core.deps import get_current_user
from ..models import user as user_models
from ..models import favorite as models
from ..schemas import favorite as schemas

router = APIRouter(prefix="/favorites", tags=["favorites"])

@router.get("/", response_model=List[schemas.Favorite])
def get_favorites(db: Session = Depends(get_db), current_user: user_models.User = Depends(get_current_user)):
    return db.query(models.Favorite).filter(models.Favorite.user_id == current_user.id).all()

@router.post("/{food_id}/", response_model=schemas.Favorite)
def add_favorite(food_id: int, db: Session = Depends(get_db), current_user: user_models.User = Depends(get_current_user)):
    db_favorite = db.query(models.Favorite).filter(
        models.Favorite.user_id == current_user.id,
        models.Favorite.food_id == food_id
    ).first()
    if db_favorite:
        return db_favorite

    db_favorite = models.Favorite(user_id=current_user.id, food_id=food_id)
    db.add(db_favorite)
    db.commit()
    db.refresh(db_favorite)
    return db_favorite

@router.delete("/{food_id}/")
def remove_favorite(food_id: int, db: Session = Depends(get_db), current_user: user_models.User = Depends(get_current_user)):
    db_favorite = db.query(models.Favorite).filter(
        models.Favorite.user_id == current_user.id,
        models.Favorite.food_id == food_id
    ).first()
    if db_favorite:
        db.delete(db_favorite)
        db.commit()
    return {"status": "success"}
