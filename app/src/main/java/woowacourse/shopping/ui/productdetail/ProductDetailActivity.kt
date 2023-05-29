package woowacourse.shopping.ui.productdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import woowacourse.shopping.R
import woowacourse.shopping.common.utils.getSerializable
import woowacourse.shopping.data.database.ShoppingDBOpenHelper
import woowacourse.shopping.data.database.dao.RecentProductDao
import woowacourse.shopping.data.recentproduct.RecentProductRepositoryImpl
import woowacourse.shopping.databinding.ActivityProductDetailBinding
import woowacourse.shopping.ui.model.ProductModel
import woowacourse.shopping.ui.model.ShoppingProductModel
import woowacourse.shopping.ui.productdetail.dialog.CartProductDialog
import woowacourse.shopping.ui.shopping.ShoppingActivity

class ProductDetailActivity : AppCompatActivity(), ProductDetailContract.View {
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var presenter: ProductDetailContract.Presenter
    private val difference: MutableList<ShoppingProductModel> by lazy {
        intent.getSerializable<ArrayList<ShoppingProductModel>>(EXTRA_KEY_DIFFERENCE)?.toMutableList() ?: mutableListOf()
    }
    private var amountDifference: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        amountDifference = intent.getIntExtra(EXTRA_KEY_AMOUNT_DIFFERENCE, 0)

        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPresenter()

        setSupportActionBar(findViewById(R.id.product_detail_toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setBackButtonPressedCallback()

        setupProductDetailCartButton()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_product_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.product_detail_close_action -> goBackToShoppingActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setupProductDetail(productModel: ProductModel) {
        binding.product = productModel
    }

    override fun setupRecentProductDetail(recentProductModel: ProductModel?) {
        binding.recentProduct = recentProductModel
        if (recentProductModel != null) {
            binding.latestRecentProductLayout.setOnClickListener {
                presenter.openProduct(recentProductModel)
            }
        }
    }

    override fun showCartProductCounter(productModel: ProductModel) {
        val dialog = CartProductDialog.createDialog(productModel)
        dialog.show(supportFragmentManager, "CartProductDialog")
    }

    override fun showProductDetail(productModel: ProductModel, recentProductModel: ProductModel?) {
        startProductDetailActivity(productModel, recentProductModel)
    }

    private fun setBackButtonPressedCallback() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    goBackToShoppingActivity()
                }
            }
        )
    }

    private fun setupProductDetailCartButton() {
        binding.productDetailCartButton.setOnClickListener {
            presenter.addToCart()
        }
    }

    private fun initPresenter() {
        val productModel = intent.getSerializable<ProductModel>(EXTRA_KEY_PRODUCT) ?: return finish()
        val recentProductModel = intent.getSerializable<ProductModel>(EXTRA_KEY_RECENT_PRODUCT)
        val db = ShoppingDBOpenHelper(this).writableDatabase
        presenter = ProductDetailPresenter(
            this,
            productModel = productModel,
            recentProductModel = recentProductModel,
            recentProductRepository = RecentProductRepositoryImpl(RecentProductDao(db))
        )
    }

    private fun startProductDetailActivity(
        productModel: ProductModel,
        recentProductModel: ProductModel?
    ) {
        val intent = createIntent(this, productModel, recentProductModel, difference, amountDifference)
        intent.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
        startActivity(intent)
        finish()
    }

    private fun goBackToShoppingActivity(): Boolean {
        val intent = ShoppingActivity.createIntent(this, difference, amountDifference)
        if (difference.isNotEmpty()) {
            setResult(RESULT_OK, intent)
        }
        finish()
        return true
    }

    fun addDifference(product: ShoppingProductModel) {
        difference.add(product)
    }

    fun addToAmountDifference(amount: Int) {
        amountDifference += amount
    }

    companion object {
        private const val EXTRA_KEY_PRODUCT = "product"
        private const val EXTRA_KEY_RECENT_PRODUCT = "recent_product"
        private const val EXTRA_KEY_DIFFERENCE = "difference"
        private const val EXTRA_KEY_AMOUNT_DIFFERENCE = "amountDifference"

        fun createIntent(
            context: Context,
            productModel: ProductModel,
            recentProductModel: ProductModel?,
            difference: List<ShoppingProductModel>,
            amountDifference: Int
        ): Intent {
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra(EXTRA_KEY_PRODUCT, productModel)
            intent.putExtra(EXTRA_KEY_RECENT_PRODUCT, recentProductModel)
            intent.putExtra(EXTRA_KEY_DIFFERENCE, ArrayList(difference))
            intent.putExtra(EXTRA_KEY_AMOUNT_DIFFERENCE, amountDifference)
            return intent
        }
    }
}
