from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from sqlalchemy import func, or_
from typing import List, Optional
from ..core.database import get_db
from ..core.deps import get_current_user
from ..models import user as user_models
from ..models import order as order_models
from ..models import restaurant as restaurant_models
from ..models import food as food_models
from ..models import address as address_models
from ..models import review as review_models
from ..models import category as category_models
from ..schemas import admin as admin_schemas
from ..schemas import order as order_schemas
from ..schemas import user as user_schemas
from ..schemas import restaurant as restaurant_schemas

router = APIRouter(prefix="/admin", tags=["admin"])

def verify_admin(current_user: user_models.User):
    if current_user.role != user_models.UserRole.ADMIN:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="The user does not have enough privileges",
        )

@router.get("/")
def test_admin_access(current_user: user_models.User = Depends(get_current_user)):
    verify_admin(current_user)
    return {"message": "Admin access granted", "admin": current_user.full_name}

@router.get("/dashboard", response_model=admin_schemas.DashboardStats)
def get_dashboard_stats(
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)

    total_users = db.query(user_models.User).filter(user_models.User.role == user_models.UserRole.CUSTOMER).count()
    total_drivers = db.query(user_models.User).filter(user_models.User.role == user_models.UserRole.DRIVER).count()
    total_restaurants = db.query(restaurant_models.Restaurant).count()
    total_foods = db.query(food_models.Food).count()
    total_orders = db.query(order_models.Order).count()

    recent_orders = (
        db.query(order_models.Order)
        .order_by(order_models.Order.created_at.desc())
        .limit(10)
        .all()
    )

    return {
        "total_users": total_users,
        "total_drivers": total_drivers,
        "total_restaurants": total_restaurants,
        "total_foods": total_foods,
        "total_orders": total_orders,
        "recent_orders": recent_orders
    }

@router.get("/orders", response_model=List[order_schemas.Order])
def get_all_orders(
    status: Optional[str] = None,
    search: Optional[str] = None,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)

    query = db.query(order_models.Order)

    if status:
        query = query.filter(order_models.Order.status == status)

    if search:
        if search.isdigit():
            query = query.filter(or_(
                order_models.Order.id == int(search),
                order_models.Order.user_id == int(search)
            ))
        else:
            query = query.join(user_models.User, user_models.User.id == order_models.Order.user_id).filter(or_(
                user_models.User.full_name.ilike(f"%{search}%"),
                user_models.User.email.ilike(f"%{search}%")
            ))

    return query.order_by(order_models.Order.created_at.desc()).all()

@router.get("/orders/{order_id}", response_model=order_schemas.Order)
def get_order_detail(
    order_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)
    db_order = db.query(order_models.Order).filter(order_models.Order.id == order_id).first()
    if not db_order:
        raise HTTPException(status_code=404, detail="Order not found")
    return db_order

@router.get("/drivers", response_model=List[admin_schemas.DriverSummary])
def get_all_drivers(
    search: Optional[str] = None,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)

    query = db.query(user_models.User).filter(user_models.User.role == user_models.UserRole.DRIVER)

    if search:
        if search.isdigit():
            query = query.filter(user_models.User.id == int(search))
        else:
            query = query.filter(or_(
                user_models.User.full_name.ilike(f"%{search}%"),
                user_models.User.email.ilike(f"%{search}%")
            ))

    drivers = query.all()
    result = []

    for d in drivers:
        completed = db.query(order_models.Order).filter(
            order_models.Order.driver_id == d.id,
            order_models.Order.status == order_models.OrderStatus.COMPLETED
        ).count()

        current = db.query(order_models.Order).filter(
            order_models.Order.driver_id == d.id,
            order_models.Order.status.notin_([
                order_models.OrderStatus.COMPLETED,
                order_models.OrderStatus.CANCELLED
            ])
        ).first()

        result.append({
            "id": d.id,
            "full_name": d.full_name,
            "email": d.email,
            "is_active": d.is_active,
            "total_completed_orders": completed,
            "current_order_id": current.id if current else None
        })

    return result

@router.get("/drivers/{driver_id}", response_model=admin_schemas.DriverDetail)
def get_driver_detail(
    driver_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)
    db_driver = db.query(user_models.User).filter(
        user_models.User.id == driver_id,
        user_models.User.role == user_models.UserRole.DRIVER
    ).first()

    if not db_driver:
        raise HTTPException(status_code=404, detail="Driver not found")

    total = db.query(order_models.Order).filter(order_models.Order.driver_id == driver_id).count()
    completed = db.query(order_models.Order).filter(
        order_models.Order.driver_id == driver_id,
        order_models.Order.status == order_models.OrderStatus.COMPLETED
    ).count()
    cancelled = db.query(order_models.Order).filter(
        order_models.Order.driver_id == driver_id,
        order_models.Order.status == order_models.OrderStatus.CANCELLED
    ).count()

    active = db.query(order_models.Order).filter(
        order_models.Order.driver_id == driver_id,
        order_models.Order.status.notin_([
            order_models.OrderStatus.COMPLETED,
            order_models.OrderStatus.CANCELLED
        ])
    ).first()

    return {
        "user": db_driver,
        "total_orders": total,
        "completed_orders": completed,
        "cancelled_orders": cancelled,
        "active_order": active
    }

@router.patch("/drivers/{driver_id}/status", response_model=user_schemas.User)
def toggle_driver_status(
    driver_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)
    db_driver = db.query(user_models.User).filter(
        user_models.User.id == driver_id,
        user_models.User.role == user_models.UserRole.DRIVER
    ).first()

    if not db_driver:
        raise HTTPException(status_code=404, detail="Driver not found")

    # Safety check: do not deactivate if they have an active order
    if db_driver.is_active:
        active_order = db.query(order_models.Order).filter(
            order_models.Order.driver_id == driver_id,
            order_models.Order.status.notin_([
                order_models.OrderStatus.COMPLETED,
                order_models.OrderStatus.CANCELLED
            ])
        ).first()
        if active_order:
             raise HTTPException(
                 status_code=400,
                 detail="Cannot deactivate driver with an active delivery"
             )

    db_driver.is_active = not db_driver.is_active
    db.commit()
    db.refresh(db_driver)
    return db_driver

@router.get("/users", response_model=List[admin_schemas.UserSummary])
def get_all_users(
    search: Optional[str] = None,
    role: Optional[str] = None,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)

    query = db.query(user_models.User).filter(user_models.User.role == user_models.UserRole.CUSTOMER)

    if role:
        if role != user_models.UserRole.CUSTOMER:
            return []

    if search:
        if search.isdigit():
            query = query.filter(user_models.User.id == int(search))
        else:
            query = query.filter(or_(
                user_models.User.full_name.ilike(f"%{search}%"),
                user_models.User.email.ilike(f"%{search}%")
            ))

    return query.order_by(user_models.User.created_at.desc()).all()

@router.get("/users/{user_id}", response_model=admin_schemas.UserDetail)
def get_user_detail(
    user_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)
    db_user = db.query(user_models.User).filter(user_models.User.id == user_id).first()

    if not db_user or db_user.role != user_models.UserRole.CUSTOMER:
        raise HTTPException(status_code=404, detail="User not found")

    total_orders = db.query(order_models.Order).filter(order_models.Order.user_id == user_id).count()
    total_reviews = db.query(review_models.Review).filter(review_models.Review.user_id == user_id).count()
    total_addresses = db.query(address_models.Address).filter(address_models.Address.user_id == user_id).count()

    return {
        "user": db_user,
        "total_orders": total_orders,
        "total_reviews": total_reviews,
        "total_addresses": total_addresses
    }

@router.get("/restaurants", response_model=List[admin_schemas.RestaurantSummary])
def get_all_restaurants(
    search: Optional[str] = None,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)

    query = db.query(restaurant_models.Restaurant)

    if search:
        query = query.filter(or_(
            restaurant_models.Restaurant.id == (int(search) if search.isdigit() else -1),
            restaurant_models.Restaurant.name.ilike(f"%{search}%"),
            restaurant_models.Restaurant.address.ilike(f"%{search}%")
        ))

    restaurants = query.all()
    result = []

    for r in restaurants:
        has_orders = (
            db.query(order_models.OrderItem)
            .join(food_models.Food, food_models.Food.id == order_models.OrderItem.food_id)
            .filter(food_models.Food.restaurant_id == r.id)
            .first() is not None
        )
        has_reviews = (
            db.query(review_models.Review)
            .join(food_models.Food, food_models.Food.id == review_models.Review.food_id)
            .filter(food_models.Food.restaurant_id == r.id)
            .first() is not None
        )
        result.append({
            "id": r.id,
            "name": r.name,
            "address": r.address,
            "open_hours": r.open_hours,
            "logo_url": r.logo_url,
            "cover_url": r.cover_url,
            "food_count": len(r.foods),
            "category_count": len(r.categories),
            "can_delete": not has_orders and not has_reviews
        })

    return result

@router.get("/restaurants/{restaurant_id}", response_model=admin_schemas.RestaurantDetailAdmin)
def get_restaurant_admin(
    restaurant_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)
    r = db.query(restaurant_models.Restaurant).filter(restaurant_models.Restaurant.id == restaurant_id).first()
    if not r:
        raise HTTPException(status_code=404, detail="Restaurant not found")

    # Calculate total orders for this restaurant
    # Order relates to food, food relates to restaurant.
    total_orders = (
        db.query(order_models.Order)
        .join(order_models.OrderItem, order_models.OrderItem.order_id == order_models.Order.id)
        .join(food_models.Food, food_models.Food.id == order_models.OrderItem.food_id)
        .filter(food_models.Food.restaurant_id == restaurant_id)
        .distinct()
        .count()
    )

    return {
        "id": r.id,
        "name": r.name,
        "address": r.address,
        "open_hours": r.open_hours,
        "logo_url": r.logo_url,
        "cover_url": r.cover_url,
        "created_at": r.created_at,
        "food_count": len(r.foods),
        "category_count": len(r.categories),
        "total_orders": total_orders,
        "can_delete": not any(
            db.query(model).join(food_models.Food, food_models.Food.id == model.food_id)
            .filter(food_models.Food.restaurant_id == restaurant_id).first()
            for model in (order_models.OrderItem, review_models.Review)
        )
    }

@router.post("/restaurants", response_model=restaurant_schemas.Restaurant)
def create_restaurant(
    restaurant_in: restaurant_schemas.RestaurantBase,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)
    if not restaurant_in.name.strip() or not restaurant_in.address.strip():
        raise HTTPException(status_code=422, detail="Restaurant name and address are required")
    db_restaurant = restaurant_models.Restaurant(
        name=restaurant_in.name.strip(),
        address=restaurant_in.address.strip(),
        open_hours=restaurant_in.open_hours,
        logo_url=restaurant_in.logo_url,
        cover_url=restaurant_in.cover_url
    )
    db.add(db_restaurant)
    db.commit()
    db.refresh(db_restaurant)
    return db_restaurant

@router.put("/restaurants/{restaurant_id}", response_model=restaurant_schemas.Restaurant)
def update_restaurant(
    restaurant_id: int,
    restaurant_in: restaurant_schemas.RestaurantBase,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)
    db_restaurant = db.query(restaurant_models.Restaurant).filter(restaurant_models.Restaurant.id == restaurant_id).first()
    if not db_restaurant:
        raise HTTPException(status_code=404, detail="Restaurant not found")

    if not restaurant_in.name.strip() or not restaurant_in.address.strip():
        raise HTTPException(status_code=422, detail="Restaurant name and address are required")

    db_restaurant.name = restaurant_in.name.strip()
    db_restaurant.address = restaurant_in.address.strip()
    db_restaurant.open_hours = restaurant_in.open_hours
    db_restaurant.logo_url = restaurant_in.logo_url
    db_restaurant.cover_url = restaurant_in.cover_url

    db.commit()
    db.refresh(db_restaurant)
    return db_restaurant

@router.delete("/restaurants/{restaurant_id}")
def delete_restaurant(
    restaurant_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)
    db_restaurant = db.query(restaurant_models.Restaurant).filter(restaurant_models.Restaurant.id == restaurant_id).first()
    if not db_restaurant:
        raise HTTPException(status_code=404, detail="Restaurant not found")

    # Preserve any data referenced by historical records or reviews.
    has_orders = (
        db.query(order_models.OrderItem)
        .join(food_models.Food, food_models.Food.id == order_models.OrderItem.food_id)
        .filter(food_models.Food.restaurant_id == restaurant_id)
        .first() is not None
    )

    if has_orders:
        raise HTTPException(
            status_code=400,
            detail="Cannot delete restaurant with existing order history. Consider removing foods instead if allowed."
        )

    has_reviews = (
        db.query(review_models.Review)
        .join(food_models.Food, food_models.Food.id == review_models.Review.food_id)
        .filter(food_models.Food.restaurant_id == restaurant_id)
        .first() is not None
    )
    if has_reviews:
        raise HTTPException(status_code=400, detail="Cannot delete restaurant with existing reviews.")

    for food in db_restaurant.foods:
        db.delete(food)
    for category in db_restaurant.categories:
        db.delete(category)

    db.delete(db_restaurant)
    db.commit()
    return {"message": "Restaurant deleted successfully"}

@router.get("/foods", response_model=List[admin_schemas.FoodSummary])
def get_all_foods(
    search: Optional[str] = None,
    restaurant_id: Optional[int] = None,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)

    query = db.query(food_models.Food)

    if restaurant_id:
        query = query.filter(food_models.Food.restaurant_id == restaurant_id)

    if search:
        query = query.filter(food_models.Food.name.ilike(f"%{search}%"))

    foods = query.all()
    result = []

    for f in foods:
        has_orders = db.query(order_models.OrderItem).filter(order_models.OrderItem.food_id == f.id).first() is not None
        has_reviews = db.query(review_models.Review).filter(review_models.Review.food_id == f.id).first() is not None
        result.append({
            "id": f.id,
            "name": f.name,
            "price": f.price,
            "is_available": f.is_available,
            "restaurant_name": f.restaurant.name if f.restaurant else "Unknown",
            "category_name": f.category.name if f.category else "None",
            "can_delete": not has_orders and not has_reviews
        })

    return result

@router.get("/foods/{food_id}", response_model=admin_schemas.FoodDetailAdmin)
def get_food_admin(
    food_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)
    f = db.query(food_models.Food).filter(food_models.Food.id == food_id).first()
    if not f:
        raise HTTPException(status_code=404, detail="Food not found")
    has_orders = db.query(order_models.OrderItem).filter(order_models.OrderItem.food_id == food_id).first() is not None
    has_reviews = db.query(review_models.Review).filter(review_models.Review.food_id == food_id).first() is not None
    return {
        "id": f.id,
        "name": f.name,
        "description": f.description,
        "price": f.price,
        "image_url": f.image_url,
        "is_available": f.is_available,
        "restaurant_id": f.restaurant_id,
        "category_id": f.category_id,
        "can_delete": not has_orders and not has_reviews
    }

@router.post("/foods", response_model=restaurant_schemas.Food)
def create_food(
    food_in: restaurant_schemas.FoodBase,
    restaurant_id: int,
    category_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)

    if not food_in.name.strip():
        raise HTTPException(status_code=422, detail="Food name is required")
    if food_in.price <= 0:
        raise HTTPException(status_code=422, detail="Food price must be greater than zero")

    # Check if restaurant exists
    rest = db.query(restaurant_models.Restaurant).filter(restaurant_models.Restaurant.id == restaurant_id).first()
    if not rest:
        raise HTTPException(status_code=404, detail="Restaurant not found")

    # Check if category exists for this restaurant
    cat = db.query(category_models.Category).filter(
        category_models.Category.id == category_id,
        category_models.Category.restaurant_id == restaurant_id
    ).first()
    if not cat:
        raise HTTPException(status_code=404, detail="Category not found for this restaurant")

    db_food = food_models.Food(
        name=food_in.name.strip(),
        description=food_in.description,
        price=food_in.price,
        image_url=food_in.image_url,
        is_available=food_in.is_available,
        restaurant_id=restaurant_id,
        category_id=category_id
    )
    db.add(db_food)
    db.commit()
    db.refresh(db_food)
    return db_food

@router.put("/foods/{food_id}", response_model=restaurant_schemas.Food)
def update_food(
    food_id: int,
    food_in: restaurant_schemas.FoodBase,
    restaurant_id: int,
    category_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)
    db_food = db.query(food_models.Food).filter(food_models.Food.id == food_id).first()
    if not db_food:
        raise HTTPException(status_code=404, detail="Food not found")

    if not food_in.name.strip():
        raise HTTPException(status_code=422, detail="Food name is required")
    if food_in.price <= 0:
        raise HTTPException(status_code=422, detail="Food price must be greater than zero")

    # Validation logic same as create
    rest = db.query(restaurant_models.Restaurant).filter(restaurant_models.Restaurant.id == restaurant_id).first()
    if not rest:
        raise HTTPException(status_code=404, detail="Restaurant not found")

    cat = db.query(category_models.Category).filter(
        category_models.Category.id == category_id,
        category_models.Category.restaurant_id == restaurant_id
    ).first()
    if not cat:
        raise HTTPException(status_code=404, detail="Category not found for this restaurant")

    db_food.name = food_in.name.strip()
    db_food.description = food_in.description
    db_food.price = food_in.price
    db_food.image_url = food_in.image_url
    db_food.is_available = food_in.is_available
    db_food.restaurant_id = restaurant_id
    db_food.category_id = category_id

    db.commit()
    db.refresh(db_food)
    return db_food

@router.delete("/foods/{food_id}")
def delete_food(
    food_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)
    db_food = db.query(food_models.Food).filter(food_models.Food.id == food_id).first()
    if not db_food:
        raise HTTPException(status_code=404, detail="Food not found")

    # Check if used in orders
    has_orders = db.query(order_models.OrderItem).filter(order_models.OrderItem.food_id == food_id).first() is not None
    has_reviews = db.query(review_models.Review).filter(review_models.Review.food_id == food_id).first() is not None
    if has_orders or has_reviews:
        raise HTTPException(status_code=400, detail="Cannot delete food referenced by order or review history. Mark it unavailable instead.")

    db.delete(db_food)
    db.commit()
    return {"message": "Food deleted successfully"}

@router.get("/categories", response_model=List[admin_schemas.CategoryAdmin])
def get_categories_by_restaurant(
    restaurant_id: int,
    db: Session = Depends(get_db),
    current_user: user_models.User = Depends(get_current_user)
):
    verify_admin(current_user)
    return db.query(category_models.Category).filter(category_models.Category.restaurant_id == restaurant_id).all()
