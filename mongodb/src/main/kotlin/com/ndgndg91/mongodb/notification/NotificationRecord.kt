package com.ndgndg91.mongodb.notification

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("notification_sends")
class NotificationRecord(
    val id: ObjectId? = null,
    val accountId: Long,
    val category: String,
    val subcategory: String,
    val notification: Notification,
    val createdAt: LocalDateTime
)