package com.ndgndg91.mongodb.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper


class LoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(LoggingFilter::class.java)
    private val objectMapper = jacksonObjectMapper()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val wrappedRequest = ContentCachingRequestWrapper(request)
        val wrappedResponse = ContentCachingResponseWrapper(response)
        val startTime = System.currentTimeMillis()

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse) // 다음 필터로 전달
        } finally {
            log.info(
                "{}", objectMapper.writeValueAsString(
                    ApiLog(
                        method = wrappedRequest.method,
                        uri = wrappedRequest.requestURI,
                        requestHeaders = getRequestHeaders(wrappedRequest),
                        requestBody = String(wrappedRequest.contentAsByteArray, Charsets.UTF_8),
                        status = wrappedResponse.status,
                        responseHeaders = getResponseHeaders(wrappedResponse),
                        responseBody = String(wrappedResponse.contentAsByteArray, Charsets.UTF_8),
                        duration = System.currentTimeMillis() - startTime
                    )
                )
            )

            wrappedResponse.copyBodyToResponse()
        }
    }

    private fun getRequestHeaders(request: HttpServletRequest): Map<String, String> {
        val reqHeaders = mutableMapOf<String, String>()
        request.headerNames.asIterator().forEachRemaining {
            reqHeaders[it] = request.getHeader(it)
        }
        return reqHeaders
    }

    private fun getResponseHeaders(response: HttpServletResponse): Map<String, String> {
        val resHeaders = mutableMapOf<String, String>()
        response.headerNames.forEach {
            resHeaders[it] = response.getHeader(it)
        }
        return resHeaders
    }
}