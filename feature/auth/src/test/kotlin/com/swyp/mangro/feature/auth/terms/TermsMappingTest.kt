package com.swyp.mangro.feature.auth.terms

import com.swyp.mangro.data.auth.model.TermsDocument
import com.swyp.mangro.data.auth.model.TermsKind
import com.swyp.mangro.feature.auth.util.consentItems
import com.swyp.mangro.feature.auth.util.kind
import org.junit.Assert.assertEquals
import org.junit.Test

class TermsMappingTest {
    @Test fun ownerDocumentsDoNotRequireConsumerOnlyConsents() {
        val documents = listOf(
            TermsDocument(7, TermsKind.SERVICE, "서비스 약관", 1, true),
            TermsDocument(8, TermsKind.PRIVACY_COLLECTION, "개인정보 수집 동의", 1, true),
            TermsDocument(9, TermsKind.MARKETING, "마케팅 동의", 1, false),
            TermsDocument(10, TermsKind.PRIVACY_POLICY, "개인정보처리방침", 1, false),
        )
        val items = documents.consentItems()
        assertEquals(listOf(TermsType.SERVICE, TermsType.PRIVACY, TermsType.MARKETING), items.map { it.type })
        assertEquals(listOf(7L, 8L, 9L), items.map { it.documentId })
    }

    @Test fun consumerDocumentsRetainAllFiveConsents() {
        val documents = TermsType.entries.mapIndexed { index, type ->
            TermsDocument(index + 1L, type.kind, type.name, 1, type.isRequired)
        } + TermsDocument(6, TermsKind.PRIVACY_POLICY, "개인정보처리방침", 1, false)
        assertEquals(TermsType.entries.toList(), documents.consentItems().map { it.type })
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyResponseDoesNotEnableSignup() {
        emptyList<TermsDocument>().consentItems()
    }
}
