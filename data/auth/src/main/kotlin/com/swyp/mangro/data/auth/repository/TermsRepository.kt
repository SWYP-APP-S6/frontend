package com.swyp.mangro.data.auth.repository

import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.TermsDocument
import kotlinx.coroutines.flow.Flow

interface TermsRepository {
    fun fetchTermsDocuments(): Flow<AuthResult<List<TermsDocument>>>

    fun fetchTermsDocument(id: Long): Flow<AuthResult<TermsDocument>>
}
