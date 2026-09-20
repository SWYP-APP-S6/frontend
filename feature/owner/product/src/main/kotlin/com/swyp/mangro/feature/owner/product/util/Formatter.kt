package com.swyp.mangro.feature.owner.product.util

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import java.text.NumberFormat
import java.util.Locale

internal fun Int.formatAmount(): String = NumberFormat.getIntegerInstance(Locale.KOREA).format(this)

internal val priceInputTransformation = InputTransformation {
    val input = asCharSequence().toString()
    val normalized = normalizePriceInput(input)
    when {
        normalized == null -> revertAllChanges()
        normalized != input -> replace(0, length, normalized)
    }
}

internal val priceOutputTransformation = OutputTransformation {
    var separatorIndex = length - 3
    while (separatorIndex > 0) {
        replace(separatorIndex, separatorIndex, ",")
        separatorIndex -= 3
    }
}
