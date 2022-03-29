package com.test.coolblue.shoppings.data

import com.test.coolblue.shoppings.ProductEntity
import kotlinx.serialization.Serializable

@Serializable
data class ProductsResponse(
  val products: List<ProductEntity>,
  val pageCount: Int
)