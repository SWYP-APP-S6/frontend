package com.swyp.mangro.data.owner.product.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.swyp.mangro.data.owner.product.model.OwnerProductFilter
import com.swyp.mangro.data.owner.product.model.ProductPage
import com.swyp.mangro.data.owner.product.model.ProductSummary
import com.swyp.mangro.data.owner.product.repository.OwnerProductRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/** Page numbers use a fixed size, including refresh requests. */
class OwnerProductPagingSource(
    private val repository: OwnerProductRepository,
    private val filter: OwnerProductFilter,
    private val onPageLoaded: (ProductPage) -> Unit = {},
) : PagingSource<Int, ProductSummary>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductSummary> = try {
        val page = params.key ?: 0
        val result = repository.fetchProducts(page, filter).first().getOrThrow()
        if (invalid) {
            LoadResult.Invalid()
        } else {
            check(result.last || result.products.isNotEmpty()) { "Non-final product page is empty" }
            onPageLoaded(result)
            LoadResult.Page(
                data = result.products,
                prevKey = (page - 1).takeIf { page > 0 },
                nextKey = if (result.last) null else page + 1,
            )
        }
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        LoadResult.Error(error)
    }

    override fun getRefreshKey(state: PagingState<Int, ProductSummary>): Int? = state.anchorPosition?.let { anchor ->
        state.closestPageToPosition(anchor)?.let { page -> page.prevKey?.plus(1) ?: page.nextKey?.minus(1) }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
