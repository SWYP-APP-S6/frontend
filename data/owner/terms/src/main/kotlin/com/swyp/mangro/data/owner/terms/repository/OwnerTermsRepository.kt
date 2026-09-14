package com.swyp.mangro.data.owner.terms.repository

import com.swyp.mangro.data.owner.terms.model.OwnerTerm
import com.swyp.mangro.data.owner.terms.model.OwnerTermDetail
import com.swyp.mangro.data.owner.terms.model.TermsResult

interface OwnerTermsRepository {
    suspend fun fetchTerms(): TermsResult<List<OwnerTerm>>
    suspend fun fetchTerm(id: Long): TermsResult<OwnerTermDetail>
}
