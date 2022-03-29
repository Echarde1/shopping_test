package com.test.coolblue.shoppings

data class ProductsState(
  val isLoading: Boolean = false,
  val query: String = "",
  val products: List<ProductEntity> = emptyList(),
  val page: Int = 0,
)