package com.ndgndg91.mongodbdocumentdb.notification.service

import com.ndgndg91.mongodbdocumentdb.notification.Notification
import com.ndgndg91.mongodbdocumentdb.notification.NotificationRecord
import com.ndgndg91.mongodbdocumentdb.notification.NotificationRecordRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class NotificationRecordService(
    private val repository: NotificationRecordRepository,
) {
    @Async("MongoDDWriterTaskExecutor")
    fun randomSave() {
        val category = randomCategory()
        val subcategory = randomSubcategory(category)
        repository.save(
            NotificationRecord(
                accountId = randomAccountId(),
                category = category,
                subcategory = subcategory,
                notification = Notification(
                    title = "제목",
                    body = "본문",
                    action = "딥링크"
                ),
                createdAt = LocalDateTime.now()
            )
        )
    }

    private fun randomAccountId(): Long {
        return (0..5_000_000L).random()
    }

    private fun randomCategory(): String {
        val categories = listOf("BASIC", "QUOTES", "COMMUNITY")
        return categories[Random.nextInt(categories.size)]
    }

    private fun randomSubcategory(category: String): String {
        val basics = listOf("TRADE", "STOP_ORDER_TRIGGER", "TRANSACTION", "PLUS_REWARD", "FEE_COUPON_EXPIRY", "LOGIN")
        val quotes = listOf(
            "RANGE_PRICE_UP",
            "RANGE_PRICE_DOWN",
            "RECENT_PRICE_HIGH",
            "RECENT_PRICE_LOW",
            "TIME_INTERVAL",
            "LIMIT_PRICE",
            "HELD_ASSETS_CHANGE"
        )
        val communities = listOf("MY_POST_LIKE", "MY_COMMENT_LIKE", "MY_FOLLOWING_ACTIVITY", "MY_NEW_FOLLOWER")
        val subcategory = when (category) {
            "BASIC" -> return basics[Random.nextInt(basics.size)]
            "QUOTES" -> return quotes[Random.nextInt(quotes.size)]
            "COMMUNITY" -> return communities[Random.nextInt(communities.size)]
            else -> "ETC"
        }
        return subcategory
    }
}