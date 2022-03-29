package com.test.coolblue.shoppings

import android.app.Application
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.test.coolblue.shoppings.ProductsApp.Provider
import com.test.coolblue.shoppings.data.ProductsApi
import com.test.coolblue.shoppings.data.ProductsCache
import com.test.coolblue.shoppings.data.ProductsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit.Builder
import retrofit2.create

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalSerializationApi
class ProductsApp : Application() {

  val productsViewModelProvider = Provider {
    ProductsViewModel(productsModule.repository)
  }

  private val productsModule = ProductsModule()

  fun interface Provider<T : Any> {

    fun get(): T

  }

  private class ProductsModule {

    companion object {

      private const val BASE_URL =
        "https://bdk0sta2n0.execute-api.eu-west-1.amazonaws.com/mobile-assignment/"

    }

    val repository = createRepository()

    private fun createRepository(): ProductsRepository {
      return ProductsRepository(
        productsApi = createProductsApi(),
        productsCache = ProductsCache()
      )
    }

    private fun createProductsApi(): ProductsApi {
      val contentType = "application/json".toMediaType()
      val json = Json {
        ignoreUnknownKeys = true
      }
      return Builder()
        .baseUrl(BASE_URL)
        .client(getOkHttpClient())
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
        .create()
    }

    private fun getOkHttpClient(): OkHttpClient {
      val loggingInterceptor = HttpLoggingInterceptor()
      loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
      val clientBuilder = OkHttpClient.Builder()
      clientBuilder.addInterceptor(loggingInterceptor)
      return clientBuilder.build()
    }
  }
}