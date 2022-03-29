package com.test.coolblue.shoppings

import android.text.Editable
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
fun ProductsViewModel.requireStateValue(): ProductsState = requireNotNull(state.value)

inline fun <reified VM : ViewModel> ComponentActivity.viewModels(
  mode: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE,
  noinline ownerProducer: () -> ViewModelStoreOwner = { this },
  crossinline creator: () -> VM,
) = lazy(mode) {
  ViewModelProvider(
    ownerProducer.invoke(),
    object : Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return creator.invoke() as T
      }
    }
  ).get(VM::class.java)
}

fun ComponentActivity.requireApp(): ProductsApp = (applicationContext as ProductsApp)

fun CharSequence?.orEmpty(): String = this?.toString() ?: ""