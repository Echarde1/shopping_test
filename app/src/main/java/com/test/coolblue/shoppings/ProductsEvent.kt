package com.test.coolblue.shoppings

sealed class ProductsEvent {

  object NewPage : ProductsEvent()

  data class NewQuery(val query: String) : ProductsEvent()

}