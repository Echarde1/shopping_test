package com.test.coolblue.shoppings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductEntity(
  @SerialName("productId")
  val id: Id,
  @SerialName("productName")
  val description: Description,
  @SerialName("salesPriceIncVat")
  val price: Price,
  @SerialName("productImage")
  val imageUrl: ImageUrl
) {

  @JvmInline
  @Serializable
  value class Id(val value: Int)

  @JvmInline
  @Serializable
  value class Description(val value: String)

  @JvmInline
  @Serializable
  value class Price(val value: Double)

  @JvmInline
  @Serializable
  value class ImageUrl(val value: String)

}