package woowacourse.shopping.presentation.ui.shopping

import android.content.Intent
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import woowacourse.shopping.R
import woowacourse.shopping.databinding.ActivityShoppingBinding
import woowacourse.shopping.presentation.base.BindingActivity
import woowacourse.shopping.presentation.ui.UiState
import woowacourse.shopping.presentation.ui.UpdateUiModel
import woowacourse.shopping.presentation.ui.ViewModelFactory
import woowacourse.shopping.presentation.ui.cart.CartActivity
import woowacourse.shopping.presentation.ui.detail.ProductDetailActivity
import woowacourse.shopping.presentation.ui.shopping.adapter.ShoppingAdapter
import woowacourse.shopping.presentation.ui.shopping.adapter.ShoppingViewType

class ShoppingActivity : BindingActivity<ActivityShoppingBinding>(), ShoppingHandler {
    override val layoutResourceId: Int
        get() = R.layout.activity_shopping

    private val viewModel: ShoppingViewModel by viewModels { ViewModelFactory() }

    private val adapter: ShoppingAdapter by lazy { ShoppingAdapter(this, viewModel) }

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun initStartView() {
        initAdapter()
        viewModel.loadProductByOffset()
        viewModel.products.observe(this) {
            when (it) {
                is UiState.None -> {}
                is UiState.Success -> {
                    adapter.updateList(it.data)
                }
            }
        }
        viewModel.errorHandler.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val updatedProducts =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            result.data?.getParcelableExtra(
                                EXTRA_UPDATED_PRODUCT,
                                UpdateUiModel::class.java,
                            )
                        } else {
                            result.data?.getParcelableExtra(EXTRA_UPDATED_PRODUCT)
                        }
                    updatedProducts?.let {
                        viewModel.updateCartProducts(it)
                    }
                }
            }
    }

    private fun initAdapter() {
        val layoutManager = GridLayoutManager(this, GRIDLAYOUT_COL)
        layoutManager.spanSizeLookup = spanManager
        binding.rvShopping.layoutManager = layoutManager
        binding.rvShopping.adapter = adapter
    }

    private val spanManager =
        object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemViewType(position) == ShoppingViewType.Product.value) {
                    ShoppingViewType.Product.span
                } else {
                    ShoppingViewType.LoadMore.span
                }
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.shopping_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        CartActivity.start(this)
        return true
    }

    override fun onClick(productId: Long) {
        activityResultLauncher.launch(
            ProductDetailActivity.createIntent(this, productId),
        )
    }

    override fun loadMore() {
        viewModel.loadProductByOffset()
    }

    companion object {
        const val GRIDLAYOUT_COL = 2
        const val EXTRA_UPDATED_PRODUCT = "updatedProduct"
    }
}
