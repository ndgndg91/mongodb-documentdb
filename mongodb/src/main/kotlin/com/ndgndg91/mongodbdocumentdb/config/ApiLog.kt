package com.ndgndg91.mongodbdocumentdb.config

data class ApiLog(
    val method: String,
    val uri: String,
    val requestHeaders: Map<String, String>,
    val requestBody: String,
    val status: Int,
    val responseHeaders: Map<String, String>,
    val responseBody: String,
    val duration: Long
)