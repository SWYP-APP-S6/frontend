package com.swyp.mangro.data.owner.terms.repository

import com.swyp.mangro.data.owner.terms.model.OwnerTerm
import com.swyp.mangro.data.owner.terms.model.OwnerTermDetail
import com.swyp.mangro.data.owner.terms.model.TermsFailure
import com.swyp.mangro.data.owner.terms.model.TermsRequirement
import com.swyp.mangro.data.owner.terms.model.TermsResult
import com.swyp.mangro.remote.auth.service.TermsService
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

internal class RemoteOwnerTermsRepository @Inject constructor(
    private val service: TermsService,
) : OwnerTermsRepository {
    override suspend fun fetchTerms(): TermsResult<List<OwnerTerm>> = request {
        val response = service.fetchTerms("OWNER")
        if (!response.isSuccessful) return@request TermsResult.Failure(TermsFailure.SERVER)
        val body = requireNotNull(response.body())
        require(body.code == "OK" && body.status == 200)
        val documents = body.data.documents.map {
            term(it.id, it.type, it.version, it.title, it.requirement, it.effectiveDate)
        }
        require(documents.isNotEmpty() && documents.map { it.id }.distinct().size == documents.size)
        TermsResult.Success(documents)
    }

    override suspend fun fetchTerm(id: Long): TermsResult<OwnerTermDetail> = request {
        require(id > 0)
        val response = service.fetchTerm(id)
        if (response.code() == 404) return@request TermsResult.Failure(TermsFailure.NOT_FOUND)
        if (!response.isSuccessful) return@request TermsResult.Failure(TermsFailure.SERVER)
        val body = requireNotNull(response.body())
        require(body.code == "OK" && body.status == 200)
        val data = body.data
        require(data.id == id && data.role == "OWNER" && data.contentMarkdown.isNotBlank())
        TermsResult.Success(
            OwnerTermDetail(
                term(data.id, data.type, data.version, data.title, data.requirement, data.effectiveDate),
                data.contentMarkdown,
            ),
        )
    }

    private fun term(id: Long, type: String, version: Int, title: String, requirement: String, effectiveDate: String): OwnerTerm {
        require(id > 0 && type.isNotBlank() && version > 0 && title.isNotBlank())
        LocalDate.parse(effectiveDate)
        return OwnerTerm(id, type, version, title, TermsRequirement.valueOf(requirement), effectiveDate)
    }

    private suspend fun <T> request(block: suspend () -> TermsResult<T>): TermsResult<T> = try {
        block()
    } catch (error: CancellationException) {
        throw error
    } catch (error: IOException) {
        TermsResult.Failure(TermsFailure.NETWORK)
    } catch (error: IllegalArgumentException) {
        TermsResult.Failure(TermsFailure.INVALID_RESPONSE)
    } catch (error: java.time.format.DateTimeParseException) {
        TermsResult.Failure(TermsFailure.INVALID_RESPONSE)
    }
}
