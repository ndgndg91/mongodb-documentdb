package com.ndgndg91.domain

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface NotificationRecordRepository: MongoRepository<NotificationRecord, ObjectId>