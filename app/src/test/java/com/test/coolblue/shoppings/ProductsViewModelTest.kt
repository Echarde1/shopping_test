package com.test.coolblue.shoppings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.test.coolblue.shoppings.ProductsEvent.NewPage
import com.test.coolblue.shoppings.ProductsEvent.NewQuery
import com.test.coolblue.shoppings.data.ProductsRepository
import com.test.coolblue.shoppings.data.ProductsRepository.GetProductsResult
import com.test.coolblue.shoppings.data.ProductsRepository.QueryAndProducts
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

@FlowPreview
@ExperimentalCoroutinesApi
class ProductsViewModelTest {

  companion object {

    private const val INITIAL_QUERY = ""
    private const val FIRST_PAGE = 1

  }

  @Rule @JvmField val coroutinesDispatcherRule = UnconfinedTestDispatcherRule()
  @Rule @JvmField val rule = InstantTaskExecutorRule()

  private val flow = MutableSharedFlow<QueryAndProducts>()
  private val repo = mockk<ProductsRepository> {
    every { getQueryFlow() } returns flow
    coEvery { emitQuery(any()) } just Runs
    coEvery { getProducts(any(), any(), any()) } returns GetProductsResult.Products(emptyList())
  }

  @Test
  fun `when viewModel init then fetch first page and subscribe to queryFlow`() = runTest {
    //given //when
    ProductsViewModel(repo)

    //then
    verify { repo.getQueryFlow() }
    coVerify { repo.getProducts(any(), INITIAL_QUERY, FIRST_PAGE) }
  }

  @Test
  fun `when NewPage event then new page with the same query`() = runTest {
    //given
    val differentQuery = "DifferentQuery"
    val newPage = 2
    coEvery { repo.emitQuery(differentQuery) } coAnswers {
      flow.emit(
        QueryAndProducts(
          products = emptyList(),
          query = differentQuery
        )
      )
    }

    // when
    val viewModel = ProductsViewModel(repo).apply {
      perform(NewQuery(differentQuery))
      perform(NewPage)
    }
    val resultQuery = viewModel.requireStateValue().query

    //then
    coVerifyOrder {
      repo.getProducts(any(), INITIAL_QUERY, FIRST_PAGE)
      repo.getProducts(any(), differentQuery, newPage)
    }
    assertNotEquals(resultQuery, INITIAL_QUERY)
    assertEquals(resultQuery, differentQuery)
  }

  // Would have been grate to test each produced state value
  @Test
  fun `when QueryTyped then update state`() = runTest {
    //given
    val query = "query"
    coEvery { repo.emitQuery(query) } coAnswers {
      flow.emit(
        QueryAndProducts(
          products = emptyList(),
          query = query
        )
      )
    }

    //when
    val viewModel = ProductsViewModel(repo).apply {
      perform(NewQuery(query))
    }
    viewModel.perform(NewQuery(query))

    //then
    coVerify { repo.emitQuery(query) }
    assertFalse(viewModel.requireStateValue().isLoading)
    assertEquals(viewModel.requireStateValue().query, query)
  }
}