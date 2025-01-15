package com.ndgndg91.mongodb.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ReadPreference
import com.mongodb.connection.ConnectionPoolSettings
import com.mongodb.connection.SocketSettings
import com.mongodb.connection.SslSettings
import org.bson.UuidRepresentation
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.ssl.DefaultSslBundleRegistry
import org.springframework.boot.ssl.SslBundle
import org.springframework.boot.ssl.SslBundles
import org.springframework.boot.ssl.SslStoreBundle
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.io.ClassPathResource
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoManagedTypes
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

@Configuration
@EnableMongoRepositories(basePackages = ["com.ndgndg91.domain"])
class MongoConfig(
    private val mongoProperties: MongoProperties,
    private val mongoConnectionDetails: MongoConnectionDetails,
    private val sslBundlesObjectProvider: ObjectProvider<SslBundles>,
): AbstractMongoClientConfiguration() {
    companion object {
        private const val MAX_CONN_POOL_SIZE = 50
        private const val MIN_CONN_POOL_SIZE = 10
        private const val MAX_CONN_LIFETIME_MIN = 1L

        private const val CONN_TIMEOUT_SEC = 3L
        private const val READ_TIMEOUT_SEC = 3L
    }

    override fun getDatabaseName(): String {
        return mongoProperties.database
    }

    override fun mongoMappingContext(
        customConversions: MongoCustomConversions,
        mongoManagedTypes: MongoManagedTypes
    ): MongoMappingContext {
        val mappingContext = MongoMappingContext()
        mappingContext.setManagedTypes(mongoManagedTypes)
        mappingContext.setSimpleTypeHolder(customConversions.simpleTypeHolder)
        mappingContext.isAutoIndexCreation = autoIndexCreation()
        return mappingContext
    }

    @Bean
    @Throws(Exception::class)
    fun rdsSslStoreBundle(): SslStoreBundle {
        val resource = ClassPathResource(mongoProperties.ssl.bundle)
        val inputStream = resource.inputStream

        // CA 번들 파일 로드
        val trustStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        trustStore.load(null)

        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val caCerts: Collection<Certificate> = cf.generateCertificates(inputStream)
        for (cert in caCerts) {
            trustStore.setCertificateEntry((cert as X509Certificate).subjectX500Principal.toString(), cert)
        }
        inputStream.close()

        return SslStoreBundle.of(null, null, trustStore)
    }

    @Bean
    fun rdsSslBundle(rdsSslStoreBundle: SslStoreBundle): SslBundle {
        return SslBundle.of(rdsSslStoreBundle)
    }

    @Bean
    fun defaultSslBundleRegistry(rdsSslBundle: SslBundle): DefaultSslBundleRegistry {
        val registry = DefaultSslBundleRegistry()
        registry.registerBundle(mongoProperties.ssl.bundle, rdsSslBundle)
        return registry
    }

    @Primary
    @Bean("mongoDbFactory")
    override fun mongoDbFactory(): MongoDatabaseFactory {
        return super.mongoDbFactory()
    }

    @Primary
    @Bean
    override fun mongoTemplate(
        mongoDbFactory: MongoDatabaseFactory,
        converter: MappingMongoConverter
    ): MongoTemplate {
        converter.setTypeMapper(DefaultMongoTypeMapper(null))
        return MongoTemplate(mongoDbFactory, converter)
    }

    @Bean("transactionMongoDatabaseFactory")
    fun transactionMongoDatabaseFactory(): MongoDatabaseFactory {
        val builder = MongoClientSettings.builder()
        this.configureClientSettings(builder)
        builder.applyConnectionString(ConnectionString(mongoProperties.uri.replace("readPreference=secondaryPreferred&", "")))
        builder.uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
        builder.readPreference(ReadPreference.primary())

        val mongoClientSettings = builder.build()
        val mongoClient = this.createMongoClient(mongoClientSettings)
        return SimpleMongoClientDatabaseFactory(mongoClient, this.databaseName)
    }

    @Bean("transactionMongoTemplate")
    fun transactionMongoTemplate(
        @Qualifier("transactionMongoDatabaseFactory") transactionMongoDatabaseFactory: MongoDatabaseFactory,
        converter: MappingMongoConverter
    ): MongoTemplate {
        converter.setTypeMapper(DefaultMongoTypeMapper(null))
        return MongoTemplate(transactionMongoDatabaseFactory, converter)
    }

    @Bean
    fun mongoTransactionManager(
        @Qualifier("transactionMongoDatabaseFactory")
        transactionMongoDatabaseFactory: MongoDatabaseFactory
    ): MongoTransactionManager {
        return MongoTransactionManager(transactionMongoDatabaseFactory)
    }

    override fun configureClientSettings(builder: MongoClientSettings.Builder) {
        builder.applyConnectionString(mongoConnectionDetails.connectionString)
            .uuidRepresentation(mongoProperties.uuidRepresentation)
            .applyToConnectionPoolSettings { connectionPoolSettingsBuilder: ConnectionPoolSettings.Builder ->
                connectionPoolSettingsBuilder
                    .maxSize(MAX_CONN_POOL_SIZE)
                    .minSize(MIN_CONN_POOL_SIZE)
                    .maxConnectionLifeTime(MAX_CONN_LIFETIME_MIN, TimeUnit.MINUTES)
            }
            .applyToSocketSettings { socketSetting: SocketSettings.Builder ->
                socketSetting
                    .connectTimeout(CONN_TIMEOUT_SEC, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            }

        if (mongoProperties.ssl.isEnabled) {
            builder.applyToSslSettings { sslSetting: SslSettings.Builder ->
                if (mongoProperties.ssl.bundle != null) {
                    sslSetting.enabled(true)
                    val sslBundles: SslBundles? = sslBundlesObjectProvider.getIfAvailable()
                    val sslBundle = sslBundles?.getBundle(mongoProperties.ssl.bundle)
                    requireNotNull(sslBundle) {
                        throw IllegalStateException("${mongoProperties.ssl.bundle} not found.")
                    }
                    require(sslBundle.options?.isSpecified == false) {
                        throw IllegalStateException("SSL options cannot be specified with MongoDB")
                    }
                    sslSetting.context(sslBundle.createSslContext())
                }
            }
        }
    }
}