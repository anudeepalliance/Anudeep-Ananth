package util.mongoDB

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.kotlinx.KotlinSerializerCodec
import org.slf4j.LoggerFactory
import util.ConfidentialObjects
import util.usage.AgentUsageLogDocument
import java.util.concurrent.TimeUnit

/**
 * Picks the in-VPS Mongo URI when `peekopals-mongo` resolves (Hostinger docker network),
 * otherwise the SSH-tunnel localhost URI for local Mac development.
 *
 * Same probe idea as PeekoPals — see `/Users/anudeepmac/IntelliJ/Peekopals/PeekoPals/server/.../mongoDB`.
 */
private fun pickMongoConnectionStringByVpsHostnameProbe(): String {
    return try {
        java.net.InetAddress.getByName("peekopals-mongo")
        ConfidentialObjects.MONGO_COMMUNITY_VPS_CONNECTION_STRING
    } catch (_: java.net.UnknownHostException) {
        ConfidentialObjects.MONGO_COMMUNITY_TUNNELED_CONNECTION_STRING
    }
}

/**
 * Typed Mongo client and collections for the `Anudeep-Ananth` database on the VPS community Mongo.
 */
object DatabaseReferences {
    private const val DATABASE_NAME = "Anudeep-Ananth"
    private const val MAX_CONNECTION_POOL_SIZE = 20

    private val kotlinxCodecRegistry = CodecRegistries.fromRegistries(
        CodecRegistries.fromCodecs(
            KotlinSerializerCodec.create<AgentUsageLogDocument>(),
        ),
        MongoClientSettings.getDefaultCodecRegistry(),
    )

    private fun mongoClientSettings(connectionString: String) = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(connectionString))
        .codecRegistry(kotlinxCodecRegistry)
        .applyToConnectionPoolSettings { connectionPoolSettings ->
            connectionPoolSettings
                .maxSize(MAX_CONNECTION_POOL_SIZE)
                .maxConnectionIdleTime(60, TimeUnit.SECONDS)
                .minSize(1)
                .maxConnectionLifeTime(30, TimeUnit.MINUTES)
                .maxWaitTime(10, TimeUnit.SECONDS)
        }
        .build()

    val mongoClient: MongoClient =
        MongoClient.create(mongoClientSettings(pickMongoConnectionStringByVpsHostnameProbe()))

    private val anudeepAnanthDatabase = mongoClient.getDatabase(DATABASE_NAME)

    val agentUsageLogsCollection: MongoCollection<AgentUsageLogDocument> =
        anudeepAnanthDatabase.getCollection(Collections.AGENT_USAGE_LOGS)
}

/**
 * Small facade for ping/close around [DatabaseReferences].
 */
object MongoDB {
    private val logger = LoggerFactory.getLogger("MongoDB")

    fun close() {
        logger.info("Closing MongoDB connection")
        DatabaseReferences.mongoClient.close()
    }

    suspend fun ping(): Boolean {
        return try {
            DatabaseReferences.mongoClient
                .getDatabase("Anudeep-Ananth")
                .runCommand(Document("ping", 1))
            true
        } catch (exception: Exception) {
            logger.error("MongoDB ping failed: ${exception.message}", exception)
            false
        }
    }
}
