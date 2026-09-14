package com.swyp.mangro.feature.auth.terms.detail

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TermsMarkdownTest {
    @Test fun markdownRendersHeadingsListsAndEmphasisWithoutExecutableHtml() {
        val html = termsHtml("# 이용 약관\n\n- **필수** 동의\n\n<script>alert(1)</script>\n\n[링크](javascript:alert(1))")
        assertTrue(html.contains("<h1>이용 약관</h1>"))
        assertTrue(html.contains("<strong>필수</strong>"))
        assertFalse(html.contains("<script>"))
        assertFalse(html.contains("href=\"javascript:"))
    }

    @Test fun privacyTablesRenderAsTables() {
        val html = termsHtml("| 항목 | 목적 |\n| --- | --- |\n| 연락처 | 안내 |")
        assertTrue(html.contains("<table>"))
        assertTrue(html.contains("<th>항목</th>"))
        assertTrue(html.contains("<td>연락처</td>"))
    }
}
