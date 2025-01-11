package com.ndgndg91.mongodbdocumentdb.notification.controller.dto.request

data class CreateNotificationRecordRequest(
    val accountId: Long,
    val title: String,
    val body: String,
    val action: String
)
