package com.swyp.mangro.feature.owner.product.util

import java.text.NumberFormat
import java.util.Locale

internal fun Int.formatAmount(): String = NumberFormat.getIntegerInstance(Locale.KOREA).format(this)
