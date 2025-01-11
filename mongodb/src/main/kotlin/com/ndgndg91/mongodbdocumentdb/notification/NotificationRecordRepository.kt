package com.ndgndg91.mongodbdocumentdb.notification

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface NotificationRecordRepository: MongoRepository<NotificationRecord, ObjectId>