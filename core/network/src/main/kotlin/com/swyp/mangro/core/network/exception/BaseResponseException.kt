package com.swyp.mangro.core.network.exception

import java.io.IOException

class BaseResponseException(val httpStatus: Int, val status: Int?, val code: String?) : IOException("Invalid or unsuccessful base response")
