package com.swyp.mangro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint

/** Hosts the post-login Owner flow without requiring a real Kakao account in UI tests. */
@AndroidEntryPoint
class OwnerNavigationTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OwnerMainContent() }
    }
}
