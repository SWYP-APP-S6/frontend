package com.swyp.mangro.data.auth.impl

import com.swyp.mangro.data.auth.BuildConfig
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.TermsDocument
import com.swyp.mangro.data.auth.model.TermsKind
import com.swyp.mangro.data.auth.repository.TermsRepository
import com.swyp.mangro.data.auth.util.authRequest
import com.swyp.mangro.data.auth.util.checked
import com.swyp.mangro.remote.auth.service.TermsService
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class TermsRepositoryImpl @Inject constructor(
    @param:Named("login") private val service: TermsService,
) : TermsRepository {

    override fun fetchTermsDocuments(): Flow<AuthResult<List<TermsDocument>>> = flow {
        val role = if (BuildConfig.IS_OWNER) {
            TermsService.RoleFetchTerms.OWNER
        } else {
            TermsService.RoleFetchTerms.CONSUMER
        }

        val result = authRequest {
            service.fetchTerms(role).checked().documents.map {
                require(it.id > 0 && it.version > 0 && it.title.isNotBlank())

                TermsDocument(
                    id = it.id,
                    type = TermsKind.valueOf(it.type.name),
                    title = it.title,
                    version = it.version,
                    required = it.requirement.name == "REQUIRED",
                )
            }.also { documents ->
                require(documents.map { it.type }.distinct().size == documents.size)
            }
        }

        emit(result)
    }.flowOn(Dispatchers.IO)

    override fun fetchTermsDocument(id: Long): Flow<AuthResult<TermsDocument>> = flow {
        val result = authRequest {
            val document = service.fetchTerm(id).checked()
            require(document.id == id && document.version > 0 && document.contentMarkdown.isNotBlank())
            require(document.role.name == if (BuildConfig.IS_OWNER) "OWNER" else "CONSUMER")

            TermsDocument(
                id = document.id,
                type = TermsKind.valueOf(document.type.name),
                title = document.title,
                version = document.version,
                required = document.requirement.name == "REQUIRED",
                content = document.contentMarkdown,
            )
        }

        emit(result)
    }.flowOn(Dispatchers.IO)
}
