package com.test.coolblue.shoppings

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.coolblue.shoppings.ProductsEvent.NewPage
import com.test.coolblue.shoppings.ProductsEvent.NewQuery
import com.test.coolblue.shoppings.data.ProductsRepository
import com.test.coolblue.shoppings.data.ProductsRepository.GetProductsResult.IgnoreCommand
import com.test.coolblue.shoppings.data.ProductsRepository.GetProductsResult.Products
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class ProductsViewModel(
  private val repository: ProductsRepository
) : ViewModel() {

  val state: LiveData<ProductsState>
    get() = _state

  private val _state = MutableLiveData(ProductsState(isLoading = true))

  init {
    onNewPage()
    subscribeOnQueryChanges()
  }

  fun perform(event: ProductsEvent) {
    when (event) {
      is NewPage -> onNewPage()
      is NewQuery -> onNewQuery(event.query)
    }
  }

  private fun onNewPage() {
    viewModelScope.launch {
      val page = requireStateValue().page + 1
      when (val result = repository
        .getProducts(
          coroutineScope = viewModelScope,
          query = requireStateValue().query,
          page = page
        )
      ) {
        is IgnoreCommand -> {
          /* all pages are loaded */
        }
        is Products -> setState {
          copy(
            isLoading = false,
            products = products + result.products,
            page = page
          )
        }
      }
    }
  }

  private fun onNewQuery(query: String) {
    if (requireStateValue().query != query) {
      setState { copy(isLoading = true, products = emptyList()) }
      viewModelScope.launch {
        repository.emitQuery(query)
      }
    }
  }

  private fun subscribeOnQueryChanges() {
    repository
      .getQueryFlow()
      .onEach { (products, query) ->
        setState {
          ProductsState(
            isLoading = false,
            products = products,
            query = query,
            page = 1
          )
        }
      }
      .launchIn(viewModelScope)
  }

  @MainThread
  private fun setState(reducer: ProductsState.() -> ProductsState) {
    val currentState = requireStateValue()
    val newState = currentState.reducer()
    if (newState != currentState) {
      _state.value = newState
    }
  }
}