package com.swyp.mangro.data.owner.product.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.swyp.mangro.data.owner.product.model.HoldPage
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.model.ManagedHold
import com.swyp.mangro.data.owner.product.repository.OwnerProductRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/** The API uses page numbers with a fixed size, including the initial request. */
class OwnerHoldPagingSource(
    private val repository: OwnerProductRepository,
    private val status: HoldStatus?,
    private val onPageLoaded: (HoldPage) -> Unit = {},
) : PagingSource<Int, ManagedHold>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ManagedHold> = try {
        val page = params.key ?: 0
        val result = repository.fetchHolds(page, status).first().getOrThrow()
        if (invalid) {
            LoadResult.Invalid()
        } else {
            check(result.last || result.holds.isNotEmpty()) { "Non-final hold page is empty" }
            onPageLoaded(result)
            LoadResult.Page(
                data = result.holds,
                prevKey = (page - 1).takeIf { page > 0 },
                nextKey = if (result.last) null else page + 1,
            )
        }
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        LoadResult.Error(error)
    }

    override fun getRefreshKey(state: PagingState<Int, ManagedHold>): Int? = state.anchorPosition?.let { anchor ->
        state.closestPageToPosition(anchor)?.let { page -> page.prevKey?.plus(1) ?: page.nextKey?.minus(1) }
    }

    companion object {
        const val PAGE_SIZE = 100
    }
}
