package com.ndgndg91.mongodbdocumentdb.notification.controller

import com.ndgndg91.mongodbdocumentdb.notification.service.NotificationRecordService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class NotificationController(
    private val service: NotificationRecordService
) {
    @PostMapping("/api/notifications")
    fun create(
    ): ResponseEntity<Void> {
        service.randomSave()
        return ResponseEntity.ok().build()
    }
}