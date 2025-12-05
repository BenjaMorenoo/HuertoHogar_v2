package com.mecagoentodo.huertohogar_v2.model

import com.mecagoentodo.huertohogar_v2.data.CartItem
import org.junit.Assert.*
import org.junit.Test

class CartItemTest {

    @Test
    fun `test fullImageUrl is constructed correctly`() {
        // Given
        val cartItem = CartItem(
            id = 1,
            productId = "test_product_id",
            productName = "Test Product",
            quantity = 2,
            price = 20.0,
            collectionId = "test_collection",
            imageUrl = "image.jpg"
        )

        // When
        val expectedUrl = "https://api-huertohogar.ironhost.cl/api/files/test_collection/test_product_id/image.jpg"

        // Then
        assertEquals(expectedUrl, cartItem.fullImageUrl)
    }

    @Test
    fun `test fullImageUrl is empty when data is missing`() {
        // Given a cart item with a blank collectionId
        val itemWithMissingCollection = CartItem(
            id = 1,
            productId = "test_product_id",
            productName = "Test Product",
            quantity = 2,
            price = 20.0,
            collectionId = "", // Blank
            imageUrl = "image.jpg"
        )

        // Given a cart item with a blank productId
        val itemWithMissingProductId = itemWithMissingCollection.copy(productId = "", collectionId = "test_collection")
        
        // Given a cart item with a blank imageUrl
        val itemWithMissingImageUrl = itemWithMissingCollection.copy(imageUrl = "", productId = "test_product_id", collectionId = "test_collection")

        // Then
        assertTrue(itemWithMissingCollection.fullImageUrl.isEmpty())
        assertTrue(itemWithMissingProductId.fullImageUrl.isEmpty())
        assertTrue(itemWithMissingImageUrl.fullImageUrl.isEmpty())
    }
}