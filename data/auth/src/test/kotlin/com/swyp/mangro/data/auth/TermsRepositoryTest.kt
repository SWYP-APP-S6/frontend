package com.swyp.mangro.data.auth

import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.data.auth.impl.TermsRepositoryImpl
import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.TermsKind
import com.swyp.mangro.remote.auth.service.TermsService
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TermsRepositoryTest {
    @Test fun requestsFlavorTermsAndReadsDetailThroughEnvelopeInterceptor() = runTest {
        MockWebServer().use { server ->
            val role = BuildConfig.FLAVOR.uppercase()
            val repository = TermsRepositoryImpl(
                NetworkModule.provideRetrofit(OkHttpClient(), NetworkModule.provideNetworkJson())
                    .newBuilder().baseUrl(server.url("/")).build().create(TermsService::class.java),
            )
            val documents = repository.fetchTermsDocuments()
            assertEquals(0, server.requestCount)
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":{"documents":[{"id":11,"type":"SERVICE","title":"서비스 약관","version":2,"requirement":"REQUIRED"}]}}"""))
            val result = documents.single() as AuthResult.Success
            assertEquals(TermsKind.SERVICE, result.value.single().type)
            assertTrue(result.value.single().required)
            assertEquals(role, server.takeRequest().requestUrl?.queryParameter("role"))
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":{"id":11,"type":"SERVICE","title":"서비스 약관","version":2,"requirement":"REQUIRED","role":"$role","contentMarkdown":"# 서비스 약관"}}"""))
            val detail = repository.fetchTermsDocument(11).single() as AuthResult.Success
            assertEquals("# 서비스 약관", detail.value.content)
            assertEquals("/terms/11", server.takeRequest().path)
        }
    }

    @Test fun rejectsOtherRoleAndMalformedTermsWithoutExposingDefaults() = runTest {
        MockWebServer().use { server ->
            val otherRole = if (BuildConfig.IS_OWNER) "CONSUMER" else "OWNER"
            val repository = TermsRepositoryImpl(
                NetworkModule.provideRetrofit(OkHttpClient(), NetworkModule.provideNetworkJson())
                    .newBuilder().baseUrl(server.url("/")).build().create(TermsService::class.java),
            )
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":{"id":11,"type":"SERVICE","title":"terms","version":1,"role":"$otherRole","contentMarkdown":"content"}}"""))
            assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repository.fetchTermsDocument(11).single())
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":{"documents":[{}]}}"""))
            assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repository.fetchTermsDocuments().single())
        }
    }
}
