package com.test.coolblue.shoppings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.test.coolblue.shoppings.ProductsActivity.ProductsAdapter.ProductViewHolder
import com.test.coolblue.shoppings.ProductsEvent.NewPage
import com.test.coolblue.shoppings.ProductsEvent.NewQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.serialization.ExperimentalSerializationApi

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalSerializationApi
class ProductsActivity : AppCompatActivity(R.layout.activity_main) {

  companion object {

    private const val ITEMS_LIMIT = 15

  }

  private val viewModel: ProductsViewModel by viewModels { requireApp().productsViewModelProvider.get() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val productsRecyclerView = initAndGetRecyclerView()
    initEditText()
    val progressBar = findViewById<ProgressBar>(R.id.progressBar)

    viewModel.state.observe(this) {
      progressBar.isVisible = it.isLoading
      productsRecyclerView.isGone = it.isLoading
      (productsRecyclerView.adapter as ProductsAdapter).submitList(it.products)
    }
  }

  private fun initAndGetRecyclerView(): RecyclerView {
    return findViewById<RecyclerView>(R.id.productsRecyclerView).apply {
      val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
      val productsAdapter = ProductsAdapter()
      adapter = productsAdapter
      layoutManager = linearLayoutManager
      addOnScrollListener(object : OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          val position = linearLayoutManager.findLastVisibleItemPosition()
          val updatePosition = productsAdapter.itemCount - 1 - (ITEMS_LIMIT / 2)
          if (position != RecyclerView.NO_POSITION && position >= updatePosition) {
            viewModel.perform(NewPage)
          }
        }
      })
    }
  }

  private fun initEditText() {
    findViewById<EditText>(R.id.queryEditText).doOnTextChanged { query, _, _, _ ->
      viewModel.perform(NewQuery(query.orEmpty()))
    }
  }

  private class ProductsAdapter : ListAdapter<ProductEntity, ProductViewHolder>(
    DiffUtilCallback()
  ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
      return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
      holder.bind(getItem(position))
    }

    private class ProductViewHolder(itemView: View) : ViewHolder(itemView) {

      private val productImage = itemView.findViewById<ImageView>(R.id.productImageView)
      private val productDescriptionTextView =
        itemView.findViewById<TextView>(R.id.productDescriptionTextView)
      private val priceTextView = itemView.findViewById<TextView>(R.id.priceTextView)

      fun bind(entity: ProductEntity) {
        entity.run {
          Glide
            .with(itemView.context)
            .load(imageUrl.value)
            .into(productImage)

          productDescriptionTextView.text = description.value
          priceTextView.text = price.value.toString()
        }
      }
    }

    private class DiffUtilCallback : DiffUtil.ItemCallback<ProductEntity>() {

      override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
        return oldItem.id == newItem.id
      }

      override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
        return oldItem == newItem
      }
    }
  }
}