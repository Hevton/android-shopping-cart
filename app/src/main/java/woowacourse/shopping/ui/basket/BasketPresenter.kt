package woowacourse.shopping.ui.basket

import woowacourse.shopping.domain.Basket
import woowacourse.shopping.domain.BasketProduct
import woowacourse.shopping.domain.Count
import woowacourse.shopping.domain.Product
import woowacourse.shopping.domain.repository.BasketRepository
import woowacourse.shopping.ui.mapper.toDomain
import woowacourse.shopping.ui.mapper.toUi
import woowacourse.shopping.ui.model.UiBasketProduct

class BasketPresenter(
    override val view: BasketContract.View,
    private val basketRepository: BasketRepository,
    private var currentPage: Int = 1,
    private var basket: Basket = Basket(basketRepository.getAll()),
    private var startId: Int = 0
) : BasketContract.Presenter {
    private val hasNext: Boolean get() = basket.products.lastIndex >= startId + BASKET_PAGING_SIZE

    override fun toggleAllProductsChecked(totalIsChecked: Boolean) {
        basket.getSubBasketByStartId(startId, BASKET_PAGING_SIZE).toggleAllCheck(totalIsChecked)
        updateOrderInformation()
        updateViewBasketProduct()
    }

    override fun updateBasketProductCheckState(basketProduct: BasketProduct) {
        basket.updateCheck(basketProduct)
        updateOrderInformation()
        view.updateTotalCheckBox(getTotalIsChecked())
    }

    private fun getTotalIsChecked(): Boolean =
        basket.getSubBasketByStartId(startId, BASKET_PAGING_SIZE).allItemIsChecked()

    private fun updateOrderInformation() {
        view.updateTotalPrice(basket.getCheckedProductsTotalPrice())
        view.updateCheckedProductsCount(basket.getCheckedProductsCount())
    }

    override fun addBasketProduct(product: Product) {
        val addedProduct = BasketProduct(count = Count(1), product = product)
        basketRepository.add(addedProduct)
        basket = basket.plus(addedProduct)
        updateBasketProductViewData()
    }

    override fun removeBasketProduct(product: Product) {
        val removedProduct = BasketProduct(count = Count(1), product = product)
        basketRepository.minus(removedProduct)
        basket = basket.minus(removedProduct)
        updateBasketProductViewData()
    }

    override fun initBasketProducts() {
        updateBasketProductViewData()
        updateOrderInformation()
    }

    override fun moveToPreviousPage() {
        currentPage -= 1
        if (startId - BASKET_PAGING_SIZE > 0) startId -= BASKET_PAGING_SIZE else startId = 0
        updatePage()
    }

    override fun moveToNextPage() {
        currentPage += 1
        if (startId + BASKET_PAGING_SIZE <= basket.products.lastIndex) startId += BASKET_PAGING_SIZE
        updatePage()
    }

    private fun updatePage() {
        view.updateCurrentPage(currentPage)
        updateBasketProductViewData()
    }

    private fun updateBasketProductViewData() {
        updateViewBasketProduct()
        view.updateNavigatorEnabled(currentPage > 1, hasNext)
    }

    private fun updateViewBasketProduct() {
        view.updateBasketProducts(
            basket.getSubBasketByStartId(
                startId,
                BASKET_PAGING_SIZE
            ).products.map { it.toUi() }
        )
    }

    override fun deleteBasketProduct(
        product: UiBasketProduct
    ) {
        basketRepository.remove(product.toDomain())
        basket = basket.remove(product.toDomain())
        amendStartId()
        updateBasketProductViewData()
    }

    private fun amendStartId() {
        if (basket.products.lastIndex < startId) {
            startId -= BASKET_PAGING_SIZE
            currentPage -= 1
            view.updateCurrentPage(currentPage)
        }
        if (startId < 0) startId = 0
        if (currentPage < 1) currentPage = 1
    }

    companion object {
        private const val BASKET_PAGING_SIZE = 5
    }
}
