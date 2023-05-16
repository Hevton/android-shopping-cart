package woowacourse.shopping.shoppingcart

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import woowacourse.shopping.model.Page
import woowacourse.shopping.model.ProductUiModel

class ShoppingCartRecyclerAdapter(
    products: List<ProductUiModel>,
    private val onRemoved: (id: Int) -> Unit,
    private val showingRule: ShowingRule,
    private val updatePageState: (pageNumber: Int, totalSize: Int) -> Unit,
    private var totalSize: Int,
) : RecyclerView.Adapter<ShoppingCartItemViewHolder>() {

    private val shoppingCartProducts: MutableList<ProductUiModel> = products.toMutableList()
    private var currentPage = Page()
    private val showingProducts: List<ProductUiModel>
        get() = showingRule.of(
            products = shoppingCartProducts,
            page = currentPage,
        )

    init {
        updatePageState(currentPage.value, totalSize)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingCartItemViewHolder {
        return ShoppingCartItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ShoppingCartItemViewHolder, position: Int) {
        holder.bind(
            productUiModel = showingProducts[position],
            onRemoveClicked = ::removeItem,
        )
    }

    private fun removeItem(position: Int) {
        onRemoved(shoppingCartProducts[position].id)
        shoppingCartProducts.removeAt(position)
        if (showingProducts.isEmpty()) {
            currentPage = currentPage.prev()
        }
        totalSize--
        updatePageState(currentPage.value, totalSize)
        notifyItemRemoved(position)
    }

    fun toNextPage(products: List<ProductUiModel>) {
        shoppingCartProducts.addAll(products)
        currentPage = currentPage.next()
        updatePageState(currentPage.value, totalSize)
        notifyItemRangeChanged(0, SHOPPING_CART_ITEM_SIZE)
    }

    fun toPreviousPage() {
        currentPage = currentPage.prev()
        updatePageState(currentPage.value, totalSize)
        notifyItemRangeChanged(0, SHOPPING_CART_ITEM_SIZE)
    }

    override fun getItemCount(): Int = showingProducts.size

    companion object {
        private const val SHOPPING_CART_ITEM_SIZE = 3
    }
}
