from sqlalchemy import Column, Integer, ForeignKey
from sqlalchemy.orm import relationship
from ..core.database import Base

class Favorite(Base):
    __tablename__ = "favorites"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    food_id = Column(Integer, ForeignKey("foods.id"))

    user = relationship("User")
    food = relationship("Food")
