package com.mecagoentodo.huertohogar_v2.viewmodel

import com.mecagoentodo.huertohogar_v2.data.repository.ProductRepository
import com.mecagoentodo.huertohogar_v2.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: HomeViewModel
    private val productRepository: ProductRepository = mock()

    private val testProducts = listOf(
        Product("1", "c1", "P1", "Apple", "Fruit", 1.0, "kg", 10.0, "", ""),
        Product("2", "c1", "P2", "Banana", "Fruit", 2.0, "kg", 20.0, "", ""),
        Product("3", "c2", "P3", "Carrot", "Vegetable", 3.0, "kg", 30.0, "", "")
    )

    @Before
    fun setUp() {
        viewModel = HomeViewModel(productRepository)
    }

    @Test
    fun `test loadProducts updates state to Success`() = runTest {
        // Given
        whenever(productRepository.getProducts()).thenReturn(testProducts)

        // When
        viewModel.loadProducts()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is ProductUiState.Success)
        val successState = uiState as ProductUiState.Success
        assertEquals(3, successState.products.size)
        assertEquals(listOf("Todas", "Fruit", "Vegetable"), successState.categories)
    }

    @Test
    fun `test loadProducts updates state to Error on exception`() = runTest {
        // Given
        val exception = RuntimeException("Failed to fetch products")
        whenever(productRepository.getProducts()).thenThrow(exception)

        // When
        viewModel.loadProducts()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is ProductUiState.Error)
        assertEquals("Error al cargar los productos: ${exception.message}", (uiState as ProductUiState.Error).message)
    }

    @Test
    fun `test selectCategory filters products correctly`() = runTest {
        // Given
        whenever(productRepository.getProducts()).thenReturn(testProducts)
        viewModel.loadProducts()
        
        // When
        viewModel.selectCategory("Fruit")

        // Then
        val uiState = viewModel.uiState.value as ProductUiState.Success
        assertEquals(2, uiState.products.size)
        assertEquals("Fruit", uiState.selectedCategory)
        assertTrue(uiState.products.all { it.category == "Fruit" })
    }

    @Test
    fun `test selectCategory for 'Todas' shows all products`() = runTest {
        // Given
        whenever(productRepository.getProducts()).thenReturn(testProducts)
        viewModel.loadProducts()
        viewModel.selectCategory("Fruit") // Pre-select a category

        // When
        viewModel.selectCategory("Todas")

        // Then
        val uiState = viewModel.uiState.value as ProductUiState.Success
        assertEquals(3, uiState.products.size)
        assertEquals("Todas", uiState.selectedCategory)
    }
}