from __future__ import annotations
import os
import sys
from datetime import datetime

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
if ROOT_DIR not in sys.path:
    sys.path.insert(0, ROOT_DIR)

from sqlalchemy import func
from sqlalchemy.orm import Session
from app.core.database import SessionLocal
from app.models import Category, Food, Restaurant, User, Address, Order, OrderItem, Payment, Review, Cart
from app.core.security import get_password_hash, verify_password
from app.models.user import UserRole
from app.models.order import OrderStatus
from app.models.payment import PaymentMethod, PaymentStatus

RESTAURANT_SEED = [
    {
        "name": "Circle K",
        "address": "456 CMT8, District 10, HCMC",
        "open_hours": "00:00 - 24:00",
        "logo_url": "ic_circle_k",
        "cover_url": "cover_circlek",
        "categories": [
            {
                "name": "Quick Snacks",
                "foods": [
                    {"name": "Hot Dog", "description": "Classic grilled hot dog with mustard and ketchup", "price": 25000, "image_url": "ic_hot_dog"},
                    {"name": "Vietnamese Banh Mi", "description": "Crispy baguette with delicious fillings", "price": 20000, "image_url": "ic_banh_mi"},
                    {"name": "Cup Noodles Nissin Seafood", "description": "Nissin seafood flavor instant cup noodles", "price": 15000, "image_url": "ic_cup_noodle_nissin_seafood"},
                    {"name": "Cup Noodles Nissin", "description": "Nissin instant cup noodles", "price": 15000, "image_url": "ic_cup_noodle_nissin"},
                    {"name": "KitKat", "description": "Crispy wafer bar covered in milk chocolate", "price": 12000, "image_url": "ic_chocolate_bar_kitkat"},
                ],
            }
        ],
    },
    {
        "name": "Highland Coffee",
        "address": "123 Le Loi, District 1, HCMC",
        "open_hours": "07:00 - 22:00",
        "logo_url": "ic_highland_coffee",
        "cover_url": "cover_highland_coffe",
        "categories": [
            {
                "name": "Coffee",
                "foods": [
                    {"name": "Black Coffee", "description": "Traditional Vietnamese iced black coffee", "price": 30000, "image_url": "ic_highland_black_coffee"},
                    {"name": "Highland Iced Coffee", "description": "Signature Highland iced coffee with cream", "price": 45000, "image_url": "ic_highland_iced_coffee"},
                    {"name": "Capuccino", "description": "Rich espresso with steamed milk foam", "price": 55000, "image_url": "ic_highland_capuccino"},
                    {"name": "Matcha Latte", "description": "Creamy and smooth green tea latte", "price": 55000, "image_url": "ic_highland_matcha_latte"},
                ],
            },
            {
                "name": "Tea & Cakes",
                "foods": [
                    {"name": "Peach Tea", "description": "Refreshing tea with real peach slices and jelly", "price": 49000, "image_url": "ic_highland_peach_tea"},
                    {"name": "Green Tea Cake", "description": "Soft green tea sponge cake with matcha cream", "price": 35000, "image_url": "ic_highland_green_tea_cake"},
                    {"name": "Coffee Mousse Cake", "description": "Delicious coffee flavored mousse cake", "price": 35000, "image_url": "ic_highland_cofee_mousse_cake"},
                ]
            }
        ],
    },
    {
        "name": "Ministop",
        "address": "789 Phan Xich Long, Phu Nhuan, HCMC",
        "open_hours": "00:00 - 24:00",
        "logo_url": "ic_ministop",
        "cover_url": "cover_ministop",
        "categories": [
            {
                "name": "Convenience Food",
                "foods": [
                    {"name": "Honey Cake", "description": "Sweet and soft traditional honey cake", "price": 15000, "image_url": "ic_honey_cake"},
                    {"name": "Potato Chips", "description": "Crispy salted classic potato chia Vietnamese bread with full fillings, a unique style, and a delicious flavor", "price": 12000, "image_url": "ic_banh_mi"},
                    {"name": "Banh Mi", "description": "Traditional food of Vietnam, vegeable", "price": 20000, "image_url": "ic_snack_potato_chips"},
                    {"name": "Chicken Curry Rice", "description": "Flavorful chicken curry served with steamed rice", "price": 45000, "image_url": "ic_chicken_curry_rice"},
                ],
            }
        ],
    },
    {
        "name": "7-Eleven",
        "address": "82 Nguyen Thi Minh Khai, District 3, HCMC",
        "open_hours": "06:00 - 23:00",
        "logo_url": "ic_7_eleven",
        "cover_url": "cover_7eleven",
        "categories": [
            {
                "name": "Drinks & Snacks",
                "foods": [
                    {"name": "Slurpee", "description": "Iconic 7-Eleven frozen carbonated beverage", "price": 25000, "image_url": "ic_soft_drink"},
                    {"name": "7-11 Milk Tea", "description": "Freshly brewed milk tea with chewy pearls", "price": 30000, "image_url": "ic_milk_tea"},
                    {"name": "Black Coffee", "description": "Strong and bold black coffee", "price": 20000, "image_url": "ic_7eleven_black_coffee"},
                ],
            }
        ],
    },
    {
        "name": "VinMart",
        "address": "101 Hoang Dieu, District 4, HCMC",
        "open_hours": "07:00 - 22:00",
        "logo_url": "ic_vinmart",
        "cover_url": "cover_vinmart",
        "categories": [
            {
                "name": "Groceries",
                "foods": [
                    {"name": "Cheese Crackers", "description": "Crispy and cheesy crackers for any time snack", "price": 12000, "image_url": "ic_cheese_crackers"},
                    {"name": "Aquafina Water", "description": "Pure bottled drinking water 500ml", "price": 7000, "image_url": "ic_bottle_water_aquafina"},
                    {"name": "Orange Juice", "description": "Fresh and tangy orange juice", "price": 25000, "image_url": "ic_orange_juice"},
                ],
            }
        ],
    },
    {
        "name": "Texas Chicken",
        "address": "12 Vo Van Tan, District 3, HCMC",
        "open_hours": "09:30 - 22:30",
        "logo_url": "ic_texas_chicken",
        "cover_url": "cover_texas",
        "categories": [
            {
                "name": "Fried Chicken",
                "foods": [
                    {"name": "Honey Cake", "description": "Sweet and soft traditional honey cake", "price": 15000, "image_url": "ic_honey_cake"},
                    {"name": "Chicken Wrap", "description": "Crispy chicken strips with fresh lettuce in a wrap", "price": 59000, "image_url": "ic_texas_chicken_wrap"},
                    {"name": "Mexicana Wrap", "description": "Spicy Mexicana style chicken wrap", "price": 59000, "image_url": "ic_texas_mexicana_wrap"},
                    {"name": "Chicken Combo", "description": "2 pieces of fried chicken, regular fries and drink", "price": 99000, "image_url": "ic_texas_combo2_chicken_potato_water"},
                    {"name": "Chicken Hamburger", "description": "Spicy chicken burger", "price": 65000, "image_url": "ic_spicy_chicken_hamburger"},
                    {"name": "Mexicana Hamburger", "description": "Spicy Mexicana style chicken burger", "price": 65000, "image_url": "ic_texas_mexicana_hamburger"},
                    {"name": "Chicken Tenders", "description": "Crispy and boneless chicken breast strips", "price": 45000, "image_url": "ic_chicken_tenders"},
                ],
            }
        ],
    },
    {
        "name": "Jollibee",
        "address": "88 Nguyen Hue, District 1, HCMC",
        "open_hours": "10:00 - 22:00",
        "logo_url": "ic_jollibee",
        "cover_url": "cover_jolibee",
        "categories": [
            {
                "name": "Menu Favorites",
                "foods": [
                    {"name": "Chicken Joy", "description": "Jollibee's signature crispy and juicy fried chicken", "price": 45000, "image_url": "ic_jollibee_chickenjoy"},
                    {"name": "Jolly Spaghetti", "description": "Sweet-style spaghetti with ham and hotdog", "price": 55000, "image_url": "ic_jollibee_spaghetti"},
                    {"name": "Peach Mango Pie", "description": "Sweet peach and mango filling in a flaky crust", "price": 25000, "image_url": "ic_jollibee_peach_mango_pie"},
                    {"name": "Spicy Chicken", "description": "Crispy fried chicken with a spicy kick", "price": 48000, "image_url": "ic_jolibee_spicy_chicken"},
                    {"name": "Chicken Tenders", "description": "Crispy and boneless chicken breast strips", "price": 45000, "image_url": "ic_chicken_tenders"},
                ],
            }
        ],
    },
]

def get_or_create_user(db: Session, email: str, full_name: str, role: str = UserRole.CUSTOMER, password: str = "password123") -> User:
    user = db.query(User).filter(User.email == email).first()
    if not user:
        user = User(
            email=email,
            full_name=full_name,
            hashed_password=get_password_hash(password),
            role=role,
            is_active=True
        )
        db.add(user)
        db.commit()
        db.refresh(user)
    else:
        if user.role != role:
            print(f"Existing demo account {email} has a different role; preserving it.")
        try:
            if not verify_password(password, user.hashed_password):
                print(f"Existing demo account {email} has a different password; preserving it.")
        except Exception:
            print(f"Existing demo account {email} password could not be checked; preserving it.")
    if not db.query(Cart).filter(Cart.user_id == user.id).first():
        db.add(Cart(user_id=user.id))
        db.commit()
    return user

def get_or_create_address(db: Session, user: User) -> Address:
    address = db.query(Address).filter(Address.user_id == user.id).first()
    if not address:
        address = Address(
            user_id=user.id,
            recipient_name=user.full_name,
            phone="0901234567",
            address_line="123 Example St, District 1, HCMC",
            is_default=True
        )
        db.add(address)
        db.commit()
        db.refresh(address)
    return address

def seed_data():
    db = SessionLocal()
    try:
        # Seed users are created only when missing; existing account settings are preserved.
        demo_user = get_or_create_user(db, "customer@example.com", "John Doe")
        driver1 = get_or_create_user(db, "driver1@foodnow.com", "Driver One", role=UserRole.DRIVER)
        driver2 = get_or_create_user(db, "driver2@foodnow.com", "Driver Two", role=UserRole.DRIVER)
        admin = get_or_create_user(db, "admin@foodnow.com", "App Admin", role=UserRole.ADMIN, password="admin123")
        demo_address = get_or_create_address(db, demo_user)

        # Seed restaurant data by names instead of database IDs.
        for rest_data in RESTAURANT_SEED:
            existing_rest = db.query(Restaurant).filter(func.lower(Restaurant.name) == rest_data["name"].lower()).first()
            if existing_rest:
                existing_rest.address = rest_data["address"]
                existing_rest.open_hours = rest_data["open_hours"]
                existing_rest.logo_url = rest_data["logo_url"]
                existing_rest.cover_url = rest_data["cover_url"]
                restaurant = existing_rest
            else:
                restaurant = Restaurant(
                    name=rest_data["name"],
                    address=rest_data["address"],
                    open_hours=rest_data["open_hours"],
                    logo_url=rest_data["logo_url"],
                    cover_url=rest_data["cover_url"]
                )
                db.add(restaurant)
            db.commit()
            db.refresh(restaurant)

            for cat_data in rest_data["categories"]:
                existing_cat = db.query(Category).filter(
                    Category.restaurant_id == restaurant.id,
                    func.lower(Category.name) == cat_data["name"].lower()
                ).first()
                if existing_cat:
                    category = existing_cat
                else:
                    category = Category(name=cat_data["name"], restaurant_id=restaurant.id)
                    db.add(category)
                db.commit()
                db.refresh(category)

                for food_data in cat_data["foods"]:
                    existing_food = db.query(Food).filter(
                        Food.restaurant_id == restaurant.id,
                        func.lower(Food.name) == food_data["name"].lower()
                    ).first()
                    if existing_food:
                        existing_food.description = food_data["description"]
                        existing_food.price = food_data["price"]
                        existing_food.image_url = food_data["image_url"]
                        existing_food.category_id = category.id
                    else:
                        food = Food(
                            name=food_data["name"],
                            description=food_data["description"],
                            price=food_data["price"],
                            image_url=food_data["image_url"],
                            restaurant_id=restaurant.id,
                            category_id=category.id,
                            is_available=True
                        )
                        db.add(food)
                    db.commit()

        # Create demo history only when each target record is missing.
        jollibee = db.query(Restaurant).filter(func.lower(Restaurant.name) == "jollibee").first()
        chicken_joy = db.query(Food).filter(Food.name == "Chicken Joy", Food.restaurant_id == jollibee.id).first() if jollibee else None
        highland = db.query(Restaurant).filter(func.lower(Restaurant.name) == "highland coffee").first()
        iced_coffee = db.query(Food).filter(Food.name == "Black Coffee", Food.restaurant_id == highland.id).first() if highland else None

        if chicken_joy and demo_address:
            completed_order = db.query(Order).join(OrderItem).filter(
                Order.user_id == demo_user.id,
                Order.status == OrderStatus.COMPLETED,
                OrderItem.food_id == chicken_joy.id
            ).first()
            if not completed_order:
                completed_order = Order(
                    user_id=demo_user.id,
                    address_id=demo_address.id,
                    total_amount=chicken_joy.price,
                    payment_method=PaymentMethod.MOMO,
                    status=OrderStatus.COMPLETED
                )
                db.add(completed_order)
                db.flush()
                db.add(OrderItem(order=completed_order, food=chicken_joy, quantity=1, price_snapshot=chicken_joy.price))
            if not db.query(Payment).filter(Payment.order_id == completed_order.id).first():
                transaction_code = f"SEED-MOMO-{completed_order.id}"
                suffix = 1
                while db.query(Payment).filter(Payment.transaction_code == transaction_code).first():
                    transaction_code = f"SEED-MOMO-{completed_order.id}-{suffix}"
                    suffix += 1
                db.add(Payment(
                    order=completed_order, method=PaymentMethod.MOMO, status=PaymentStatus.PAID,
                    amount=chicken_joy.price, transaction_code=transaction_code, paid_at=datetime.utcnow()
                ))
            db.commit()

            if not db.query(Review).filter(Review.user_id == demo_user.id, Review.food_id == chicken_joy.id).first():
                db.add(Review(
                    user_id=demo_user.id,
                    food_id=chicken_joy.id,
                    rating=5.0,
                    comment="The chicken was so crispy and hot! Loved it."
                ))
                db.commit()

        if iced_coffee and demo_address:
            order2 = db.query(Order).join(OrderItem).filter(
                Order.user_id == demo_user.id,
                Order.status == OrderStatus.WAITING_FOR_DRIVER,
                OrderItem.food_id == iced_coffee.id
            ).first()
            if not order2:
                order2 = Order(
                    user_id=demo_user.id,
                    address_id=demo_address.id,
                    total_amount=iced_coffee.price,
                    payment_method=PaymentMethod.COD,
                    status=OrderStatus.WAITING_FOR_DRIVER
                )
                db.add(order2)
                db.flush()
                db.add(OrderItem(order=order2, food=iced_coffee, quantity=1, price_snapshot=iced_coffee.price))
            if not db.query(Payment).filter(Payment.order_id == order2.id).first():
                db.add(Payment(order=order2, method=PaymentMethod.COD, status=PaymentStatus.PENDING, amount=iced_coffee.price))
            db.commit()

        print("Seed data refined successfully with English content and demo records.")
    finally:
        db.close()

if __name__ == "__main__":
    seed_data()
