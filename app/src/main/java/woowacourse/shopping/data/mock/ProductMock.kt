package woowacourse.shopping.data.mock

import woowacourse.shopping.domain.Product
import woowacourse.shopping.domain.URL

object ProductMock {
    fun make(id: Int): Product = Product(
        id,
        URL("https://picsum.photos/seed/picsum/200/300"),
        (0..100).random().toString(),
        10000
    )
}
