package com.ndgndg91.mongodb.notification

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface NotificationRecordRepository: MongoRepository<NotificationRecord, ObjectId>