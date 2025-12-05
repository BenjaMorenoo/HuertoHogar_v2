package com.mecagoentodo.huertohogar_v2.model

import org.junit.Assert.*
import org.junit.Test

class ProductTest {

    @Test
    fun `test fullImageUrl is constructed correctly`() {
        // Given
        val product = Product(
            id = "test_id",
            collectionId = "test_collection",
            code = "P001",
            name = "Test Product",
            category = "Test Category",
            price = 10.0,
            unit = "unit",
            stock = 100.0,
            description = "A test product",
            imageUrl = "image.jpg"
        )

        // When
        val expectedUrl = "https://api-huertohogar.ironhost.cl/api/files/test_collection/test_id/image.jpg"
        
        // Then
        assertEquals(expectedUrl, product.fullImageUrl)
    }

    @Test
    fun `test fullImageUrl is empty when data is missing`() {
        // Given a product with a blank collectionId
        val productWithMissingCollection = Product(
            id = "test_id",
            collectionId = "", // Blank collectionId
            code = "P001",
            name = "Test Product",
            category = "Test Category",
            price = 10.0,
            unit = "unit",
            stock = 100.0,
            description = "A test product",
            imageUrl = "image.jpg"
        )

        // Given a product with a blank id
        val productWithMissingId = productWithMissingCollection.copy(id = "", collectionId = "test_collection")
        
        // Given a product with a blank imageUrl
        val productWithMissingImageUrl = productWithMissingCollection.copy(imageUrl = "", id = "test_id", collectionId = "test_collection")

        // Then
        assertTrue(productWithMissingCollection.fullImageUrl.isEmpty())
        assertTrue(productWithMissingId.fullImageUrl.isEmpty())
        assertTrue(productWithMissingImageUrl.fullImageUrl.isEmpty())
    }
}