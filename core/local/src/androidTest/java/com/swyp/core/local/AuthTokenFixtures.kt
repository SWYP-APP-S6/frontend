package com.swyp.core.local

internal object AuthTokenFixtures {
    // Synthetic, expired JWT fixtures signed with a test-only key; not server-issued credentials.
    const val ACCESS_TOKEN =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJzdWIiOiIxMDAwMSIsInJvbGUiOiJPV05FUiIsInRva2VuX3R5cGUiOiJhY2Nlc3MiLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6MTcwMDAwMzYwMCwianRpIjoidGVzdC1hY2Nlc3MtMTAwMDEifQ." +
            "3hPP6758qnAg8jZyEesl7Qk6FIq_aGaqQ9ppkbJACEM"
    const val REFRESH_TOKEN =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJzdWIiOiIxMDAwMSIsInJvbGUiOiJPV05FUiIsInRva2VuX3R5cGUiOiJyZWZyZXNoIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDEyMDk2MDAsImp0aSI6InRlc3QtcmVmcmVzaC0xMDAwMSJ9." +
            "al_cB9Ik7Paxm9sojTHAzeUM8M8ou6OtPGMw3SPVqxg"
}
