package com.mecagoentodo.huertohogar_v2.data.repository

import android.content.Context
import android.net.Uri
import com.mecagoentodo.huertohogar_v2.data.network.ApiService
import com.mecagoentodo.huertohogar_v2.model.Product
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

open class ProductRepository(private val apiService: ApiService) {

    open suspend fun getProducts(): List<Product> {
        return apiService.getProducts().items
    }

    open suspend fun getProductById(productId: String): Product {
        return apiService.getProductById(productId)
    }

    open suspend fun createProduct(context: Context, productData: Map<String, Any>, imageUri: Uri?): Product {
        val parts = productData.mapValues {
            it.value.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        }

        var imagePart: MultipartBody.Part? = null
        if (imageUri != null) {
            val file = uriToFile(context, imageUri)
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            imagePart = MultipartBody.Part.createFormData("imageUrl", file.name, requestFile)
        }

        return apiService.createProduct(parts, imagePart)
    }

    open suspend fun updateProduct(context: Context, productId: String, data: Map<String, Any>, imageUri: Uri?): Product {
         val parts = data.mapValues {
            it.value.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        }

        var imagePart: MultipartBody.Part? = null
        if (imageUri != null) {
            val file = uriToFile(context, imageUri)
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            imagePart = MultipartBody.Part.createFormData("imageUrl", file.name, requestFile)
        }

        return apiService.updateProduct(productId, parts, imagePart)
    }

    open suspend fun deleteProduct(productId: String) {
        apiService.deleteProduct(productId)
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val file = File(context.cacheDir, "temp_image.jpg")
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return file
    }
}