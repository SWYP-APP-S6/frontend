package com.swyp.mangro.data.owner.product.impl

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

internal interface ProductPhotoSource {
    fun part(uri: String): MultipartBody.Part
}

internal class ContentProductPhotoSource @Inject constructor(@param:ApplicationContext private val context: Context) : ProductPhotoSource {
    override fun part(uri: String): MultipartBody.Part {
        val sourceUri = uri.toUri()
        require(sourceUri.scheme == "content")
        val resolver = context.contentResolver
        val mimeType = requireNotNull(resolver.getType(sourceUri))
        require(mimeType.startsWith("image/"))
        val body = object : RequestBody() {
            override fun contentType() = mimeType.toMediaType()
            override fun writeTo(sink: BufferedSink) {
                requireNotNull(resolver.openInputStream(sourceUri)).source().use { sink.writeAll(it) }
            }
        }
        return MultipartBody.Part.createFormData("file", "product.${mimeType.substringAfter('/').substringBefore('+')}", body)
    }
}
