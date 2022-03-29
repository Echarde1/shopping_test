package com.test.coolblue.shoppings.data

class ProductsCache {

  companion object {

    const val NO_LOADED_PAGES = -1

  }

  private var pageCount = NO_LOADED_PAGES

  fun setPageCount(pageCount: Int) {
    this.pageCount = pageCount
  }

  fun getPageCount() = pageCount

}