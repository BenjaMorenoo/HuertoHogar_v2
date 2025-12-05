package com.mecagoentodo.huertohogar_v2.viewmodel

import android.app.Application
import com.mecagoentodo.huertohogar_v2.data.repository.ProductRepository
import com.mecagoentodo.huertohogar_v2.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class AdminViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AdminViewModel
    private val productRepository: ProductRepository = mock()
    private val application: Application = mock()

    // Create a real, dummy Product for tests
    private val dummyProduct = Product("1", "c1", "P1", "Test Product", "Test Category", 1.0, "kg", 10.0, "", "")

    @Before
    fun setUp() {
        viewModel = AdminViewModel(application, productRepository)
    }

    @Test
    fun `test createProduct updates state to Success on repository success`() = runTest {
        // Given
        val productData = mapOf("name" to "Test Product")
        // Use the real dummyProduct instead of mock()
        whenever(productRepository.createProduct(any(), any(), any())).thenReturn(dummyProduct)

        // When
        viewModel.createProduct(productData, null)

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is AdminUiState.Success)
        assertEquals("¡Producto creado con éxito!", (uiState as AdminUiState.Success).message)
    }

    @Test
    fun `test createProduct updates state to Error on HttpException`() = runTest {
        // Given
        val productData = mapOf("name" to "Test Product")
        val errorBody = "{\"message\":\"Invalid data provided\"}".toResponseBody("application/json".toMediaTypeOrNull())
        val httpException = HttpException(Response.error<Any>(400, errorBody))
        whenever(productRepository.createProduct(any(), any(), any())).thenThrow(httpException)

        // When
        viewModel.createProduct(productData, null)

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is AdminUiState.Error)
        val errorMessage = (uiState as AdminUiState.Error).message
        assertTrue(errorMessage.contains("Error HTTP 400"))
        assertTrue(errorMessage.contains("Invalid data provided"))
    }
}