package woowacourse.shopping.feature.main

import com.example.domain.model.Product
import com.example.domain.model.RecentProduct
import com.example.domain.repository.CartRepository
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.RecentProductRepository
import woowacourse.shopping.mapper.toDomain
import woowacourse.shopping.mapper.toPresentation
import woowacourse.shopping.model.ProductUiModel
import java.time.LocalDateTime

class MainPresenter(
    private val view: MainContract.View,
    private val productRepository: ProductRepository,
    private val recentProductRepository: RecentProductRepository,
    private val cartRepository: CartRepository
) : MainContract.Presenter {

    private var totalCount: Int = cartRepository.getAll().size
    private val mainProducts = mutableListOf<ProductUiModel>()
    private var page = 1

    override fun loadProducts() {
        productRepository.getProducts(
            page = page,
            onSuccess = {
                ++page
                mainProducts.addAll(matchCartProductCount(it))
                view.submitList(mainProducts)
            }
        )
    }

    private fun matchCartProductCount(products: List<Product>): List<ProductUiModel> {
        val cartProducts = cartRepository.getAll()
        return products.map { product ->
            val count = cartProducts.find { it.product.id == product.id }?.count ?: 0
            product.toPresentation(count)
        }
    }

    override fun loadRecent() {
        val recent = recentProductRepository.getAll().map { it.toPresentation() }
        view.updateRecent(recent)
    }

    override fun setCartProductCount() {
        val count = cartRepository.getAll().size
        view.updateCartProductCount(count)
    }

    override fun moveToCart() {
        view.showCartScreen()
    }

    override fun moveToDetail(product: ProductUiModel) {
        addRecentProduct(RecentProduct(product.toDomain(), LocalDateTime.now()))
        loadRecent()
        view.showProductDetailScreenByProduct(
            product,
            recentProductRepository.getMostRecentProduct()?.product?.toPresentation()
        )
    }

    private fun addRecentProduct(recentProduct: RecentProduct) {
        recentProductRepository.addRecentProduct(recentProduct.copy(dateTime = LocalDateTime.now()))
    }

    override fun refresh() {
        productRepository.clearCache()
    }

    override fun increaseCartProduct(product: ProductUiModel, previousCount: Int) {
        cartRepository.addProduct(product.toDomain(), previousCount + 1)
        if (previousCount == 0) totalCount++
        view.updateCartProductCount(totalCount)
        val index = mainProducts.indexOfFirst { it.id == product.id }
        if (index != -1) {
            mainProducts[index] = mainProducts[index].copy(count = previousCount + 1)
        }
    }

    override fun decreaseCartProduct(product: ProductUiModel, previousCount: Int) {
        if (previousCount == 1) {
            cartRepository.deleteProduct(product.toDomain())
            totalCount--
            view.updateCartProductCount(totalCount)
        } else {
            cartRepository.addProduct(product.toDomain(), previousCount - 1)
        }
        val index = mainProducts.indexOfFirst { it.id == product.id }
        if (index != -1) {
            mainProducts[index] = mainProducts[index].copy(count = previousCount - 1)
        }
    }

    override fun updateProducts() {
        val products = cartRepository.getAll().map {
            it.product.toPresentation(count = it.count)
        }
        view.updateCartProductCount(products.size)
        if (mainProducts.isEmpty()) return

        mainProducts.forEachIndexed { index, product ->
            if (product.count != 0) {
                mainProducts[index] = mainProducts[index].copy(
                    count = products.find { it.id == product.id }?.count ?: 0
                )
            }
        }
        view.submitList(mainProducts)
        view.updateCartProductCount(products.size)
    }
}
