package com.mecagoentodo.huertohogar_v2.data.network

import com.mecagoentodo.huertohogar_v2.data.User
import com.mecagoentodo.huertohogar_v2.data.network.model.AuthResponse
import com.mecagoentodo.huertohogar_v2.data.network.model.LoginRequest
import com.mecagoentodo.huertohogar_v2.data.network.model.RegisterRequest
import com.mecagoentodo.huertohogar_v2.model.Product
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Productos ---
    @GET("collections/products/records")
    suspend fun getProducts(): PocketBaseResponse<Product>

    @GET("collections/products/records/{id}")
    suspend fun getProductById(@Path("id") productId: String): Product

    @Multipart
    @POST("collections/products/records")
    suspend fun createProduct(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Product

    @Multipart
    @PATCH("collections/products/records/{id}")
    suspend fun updateProduct(
        @Path("id") productId: String,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Product

    @DELETE("collections/products/records/{id}")
    suspend fun deleteProduct(@Path("id") productId: String): Response<Unit>

    // --- Autenticación y Usuarios ---
    @POST("collections/users/auth-with-password")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("collections/users/records")
    suspend fun register(@Body request: RegisterRequest): User

    @PATCH("collections/users/records/{id}")
    suspend fun updateUser(@Path("id") userId: String, @Body user: Map<String, @JvmSuppressWildcards Any>): User
}