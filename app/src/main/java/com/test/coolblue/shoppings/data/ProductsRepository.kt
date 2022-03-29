package com.test.coolblue.shoppings.data

import com.test.coolblue.shoppings.ProductEntity
import com.test.coolblue.shoppings.data.ProductsRepository.GetProductsResult.IgnoreCommand
import com.test.coolblue.shoppings.data.ProductsRepository.GetProductsResult.Products
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow.DROP_LATEST
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import java.util.concurrent.ConcurrentHashMap

class ProductsRepository(
  private val productsApi: ProductsApi,
  private val productsCache: ProductsCache
) {

  companion object {

    private const val QUERY_CHANGES_TIMEOUT = 500L

    private const val FIRST_PAGE = 1

  }

  private val queryFlow = MutableSharedFlow<String>()
  private val pageLoadsJobs: MutableMap<QueryAndPage, Job> = ConcurrentHashMap<QueryAndPage, Job>()

  suspend fun getProducts(
    coroutineScope: CoroutineScope,
    query: String,
    page: Int
  ): GetProductsResult {
    val isJobLoading = pageLoadsJobs.get(QueryAndPage(query, page))?.isActive  ?: false
    return if (!isJobLoading && shouldLoadMoreProducts(page)) {
      val job = coroutineScope.async(start = CoroutineStart.LAZY) {
        productsApi.fetchProducts(query, page)
      }
      pageLoadsJobs[QueryAndPage(query, page)] = job
      val response = job.await()
      val products = response.products
      productsCache.setPageCount(response.pageCount)
      Products(products = products)
    } else {
      IgnoreCommand
    }
  }

  private fun shouldLoadMoreProducts(
    page: Int
  ): Boolean {
    val pageCount = productsCache.getPageCount()
    return pageCount == -1 || page <= pageCount
  }

  @FlowPreview
  @ExperimentalCoroutinesApi
  fun getQueryFlow(): Flow<QueryAndProducts> = queryFlow
    .debounce(QUERY_CHANGES_TIMEOUT)
    .distinctUntilChanged()
    .mapLatest { query ->
      val response = productsApi.fetchProducts(query, FIRST_PAGE)
      productsCache.setPageCount(response.pageCount)
      QueryAndProducts(
        products = response.products,
        query = query
      )
    }

  suspend fun emitQuery(query: String) {
    queryFlow.emit(query)
  }

  data class QueryAndProducts(
    val products: List<ProductEntity>,
    val query: String
  )

  data class QueryAndPage(
    val query: String,
    val page: Int
  )

  sealed class GetProductsResult {

    object IgnoreCommand : GetProductsResult()

    data class Products(val products: List<ProductEntity>) : GetProductsResult()

  }
}