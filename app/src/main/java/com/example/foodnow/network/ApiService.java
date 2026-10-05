package com.example.foodnow.network;

import com.example.foodnow.model.AuthResponse;
import com.example.foodnow.model.Restaurant;
import com.example.foodnow.model.RegisterRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @FormUrlEncoded
    @POST("auth/login")
    Call<AuthResponse> login(
        @Field("username") String email,
        @Field("password") String password
    );

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @GET("restaurants/")
    Call<List<Restaurant>> getRestaurants();

    @GET("restaurants/{id}/")
    Call<Restaurant> getRestaurantDetail(@Path("id") int id);

    @GET("cart/")
    Call<com.example.foodnow.model.Cart> getCart();

    @POST("cart/items/")
    Call<com.example.foodnow.model.CartItem> addToCart(@Body com.example.foodnow.model.CartItemRequest request);

    @PUT("cart/items/{item_id}/")
    Call<com.example.foodnow.model.CartItem> updateCartItem(@Path("item_id") int itemId, @Body com.example.foodnow.model.CartItemRequest request);

    @DELETE("cart/items/{item_id}/")
    Call<Void> deleteCartItem(@Path("item_id") int itemId);

    @GET("addresses/")
    Call<java.util.List<com.example.foodnow.model.Address>> getAddresses();

    @POST("addresses/")
    Call<com.example.foodnow.model.Address> addAddress(@Body com.example.foodnow.model.Address address);

    @PUT("addresses/{id}/")
    Call<com.example.foodnow.model.Address> updateAddress(@Path("id") int id, @Body com.example.foodnow.model.Address address);

    @DELETE("addresses/{id}/")
    Call<Void> deleteAddress(@Path("id") int id);

    @POST("orders/")
    Call<com.example.foodnow.model.Order> createOrder(@Body com.example.foodnow.model.OrderRequest request);

    @GET("orders/")
    Call<java.util.List<com.example.foodnow.model.Order>> getOrders();

    @GET("orders/{id}/")
    Call<com.example.foodnow.model.Order> getOrder(@Path("id") int orderId);

    @POST("orders/{id}/cancel/")
    Call<com.example.foodnow.model.Order> cancelOrder(@Path("id") int orderId);

    @PUT("orders/{order_id}/items/{item_id}/")
    Call<com.example.foodnow.model.Order> updateOrderItem(@Path("order_id") int orderId, @Path("item_id") int itemId, @Body com.example.foodnow.model.OrderItemUpdate request);

    @DELETE("orders/{order_id}/items/{item_id}/")
    Call<com.example.foodnow.model.Order> deleteOrderItem(@Path("order_id") int orderId, @Path("item_id") int itemId);

    @GET("payments/{id}/")
    Call<com.example.foodnow.model.Payment> getPayment(@Path("id") int paymentId);

    @POST("payments/{id}/confirm/")
    Call<com.example.foodnow.model.Payment> confirmPayment(@Path("id") int paymentId, @Body com.example.foodnow.model.PaymentConfirmRequest request);

    @GET("favorites/")
    Call<java.util.List<com.example.foodnow.model.Favorite>> getFavorites();

    @POST("favorites/{food_id}/")
    Call<com.example.foodnow.model.Favorite> addFavorite(@Path("food_id") int foodId);

    @DELETE("favorites/{food_id}/")
    Call<Void> removeFavorite(@Path("food_id") int foodId);

    @GET("reviews/{food_id}/")
    Call<java.util.List<com.example.foodnow.model.Review>> getReviews(@Path("food_id") int foodId);

    @GET("reviews/{food_id}/eligibility/")
    Call<com.example.foodnow.model.ReviewEligibility> getReviewEligibility(@Path("food_id") int foodId);

    @POST("reviews/")
    Call<com.example.foodnow.model.Review> createReview(@Body com.example.foodnow.model.ReviewRequest reviewRequest);

    @GET("users/me/")
    Call<com.example.foodnow.model.User> getMe();

    @PUT("users/me/")
    Call<com.example.foodnow.model.User> updateProfile(@Body com.example.foodnow.model.User user);

    // Driver Endpoints
    @GET("driver/orders/available")
    Call<List<com.example.foodnow.model.Order>> getAvailableOrders();

    @GET("driver/orders/active")
    Call<List<com.example.foodnow.model.Order>> getActiveOrders();

    @GET("driver/orders/history")
    Call<List<com.example.foodnow.model.Order>> getDriverOrderHistory();

    @POST("driver/orders/{order_id}/accept")
    Call<com.example.foodnow.model.Order> acceptOrder(@Path("order_id") int orderId);

    @POST("driver/orders/{order_id}/status")
    Call<com.example.foodnow.model.Order> updateOrderStatus(@Path("order_id") int orderId, @Body com.example.foodnow.model.StatusUpdate statusUpdate);
}
