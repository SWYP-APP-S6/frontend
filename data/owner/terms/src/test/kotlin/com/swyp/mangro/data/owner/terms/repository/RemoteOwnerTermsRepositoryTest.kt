package com.swyp.mangro.data.owner.terms.repository

import com.swyp.mangro.core.network.NetworkClient
import com.swyp.mangro.data.owner.terms.model.TermsFailure
import com.swyp.mangro.data.owner.terms.model.TermsRequirement
import com.swyp.mangro.data.owner.terms.model.TermsResult
import com.swyp.mangro.remote.auth.service.TermsService
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteOwnerTermsRepositoryTest {
    private val server = MockWebServer()
    private val repository by lazy { RemoteOwnerTermsRepository(NetworkClient.create(server.url("/").toString()).create(TermsService::class.java)) }

    @After fun tearDown() {
        server.shutdown()
    }

    private fun document(id: Long, requirement: String) = """{"id":$id,"type":"SERVICE","version":1,"title":"약관 $id","requirement":"$requirement","effectiveDate":"2026-09-16"}"""
    private fun envelope(data: String) = """{"status":200,"code":"OK","message":"ok","data":$data}"""
    private fun enqueue(data: String) {
        server.enqueue(MockResponse().setBody(envelope(data)))
    }

    @Test fun listUsesOwnerRoleWithoutAuthorizationAndPreservesServerOrder() = runTest {
        enqueue("""{"documents":[${document(9, "OPTIONAL")},${document(7, "REQUIRED")},${document(10, "NOTICE")}]}""")
        val result = repository.fetchTerms() as TermsResult.Success
        assertEquals(listOf(9L, 7L, 10L), result.value.map { it.id })
        assertEquals(TermsRequirement.NOTICE, result.value.last().requirement)
        val request = server.takeRequest()
        assertEquals("/terms?role=OWNER", request.path)
        assertNull(request.getHeader("Authorization"))
    }

    @Test fun detailUsesDocumentIdAndReturnsMarkdown() = runTest {
        val detail = document(7, "REQUIRED").dropLast(1) + """, "role":"OWNER","contentMarkdown":"# 이용 약관"}"""
        enqueue(detail)
        val result = repository.fetchTerm(7) as TermsResult.Success
        assertEquals("# 이용 약관", result.value.contentMarkdown)
        assertEquals("/terms/7", server.takeRequest().path)
    }

    @Test fun missingDocumentMapsToNotFound() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"code":"TERMS_DOCUMENT_NOT_FOUND"}"""))
        assertEquals(TermsResult.Failure(TermsFailure.NOT_FOUND), repository.fetchTerm(7))
    }

    @Test fun unknownRequirementAndMissingFieldsNeverBecomeAgreedDocuments() = runTest {
        enqueue("""{"documents":[${document(7, "NEW_REQUIREMENT")}]}""")
        assertEquals(TermsResult.Failure(TermsFailure.INVALID_RESPONSE), repository.fetchTerms())
        enqueue("""{"documents":[{}]}""")
        assertEquals(TermsResult.Failure(TermsFailure.INVALID_RESPONSE), repository.fetchTerms())
    }

    @Test fun emptyAndDuplicateDocumentsAreRejected() = runTest {
        enqueue("""{"documents":[]}""")
        assertEquals(TermsResult.Failure(TermsFailure.INVALID_RESPONSE), repository.fetchTerms())
        enqueue("""{"documents":[${document(7, "REQUIRED")},${document(7, "REQUIRED")}]}""")
        assertEquals(TermsResult.Failure(TermsFailure.INVALID_RESPONSE), repository.fetchTerms())
    }

    @Test fun mismatchedDetailAndServerFailuresAreRejected() = runTest {
        enqueue(document(8, "REQUIRED").dropLast(1) + """, "role":"OWNER","contentMarkdown":"본문"}""")
        assertEquals(TermsResult.Failure(TermsFailure.INVALID_RESPONSE), repository.fetchTerm(7))
        server.enqueue(MockResponse().setResponseCode(503))
        assertEquals(TermsResult.Failure(TermsFailure.SERVER), repository.fetchTerms())
    }
}
