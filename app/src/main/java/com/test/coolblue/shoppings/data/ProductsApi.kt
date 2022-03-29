package com.test.coolblue.shoppings.data

import retrofit2.http.GET
import retrofit2.http.Query

interface ProductsApi {

  @GET("search")
  suspend fun fetchProducts(
    @Query("query") query: String,
    @Query("page") page: Int
  ): ProductsResponse

}