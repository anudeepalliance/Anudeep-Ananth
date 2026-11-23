package com.anudeepananth.projects.util.mongoDB

import com.anudeepananth.projects.ocrolus.OcrolusAccount
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.OcrolusModelCallLog
import com.anudeepananth.projects.util.ConfidentialObjects
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.kotlinx.KotlinSerializerCodec
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Picks the MongoDB address: the in-VPS one when the `peekopals-mongo` container name resolves
 * (we are running on the server), otherwise the SSH-tunnel address for the Mac.
 */
private fun pickMongoConnectionStringByVpsHostnameProbe(): String {
    return try {
        InetAddress.getByName("peekopals-mongo")
        ConfidentialObjects.MONGO_COMMUNITY_VPS_CONNECTION_STRING
    } catch (_: UnknownHostException) {
        ConfidentialObjects.MONGO_COMMUNITY_TUNNELED_CONNECTION_STRING
    }
}

/** The one MongoDB client for this server, plus typed handles to each collection it uses. */
object DatabaseReferences {
    private const val DATABASE_NAME = "Anudeep-Ananth"

    private val kotlinxCodecRegistry = CodecRegistries.fromRegistries(
        CodecRegistries.fromCodecs(
            KotlinSerializerCodec.create<OcrolusAccount>(),
            KotlinSerializerCodec.create<OcrolusAssignmentRun>(),
            KotlinSerializerCodec.create<OcrolusModelCallLog>(),
        ),
        MongoClientSettings.getDefaultCodecRegistry(),
    )

    val mongoClient: MongoClient = MongoClient.create(
        MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(pickMongoConnectionStringByVpsHostnameProbe()))
            .codecRegistry(kotlinxCodecRegistry)
            .applyToConnectionPoolSettings { connectionPoolSettings ->
                connectionPoolSettings
                    .maxSize(10)
                    .minSize(1)
                    .maxConnectionIdleTime(60, TimeUnit.SECONDS)
                    .maxWaitTime(10, TimeUnit.SECONDS)
            }
            .build(),
    )

    private val anudeepAnanthDatabase = mongoClient.getDatabase(DATABASE_NAME)

    val ocrolusAccountsCollection: MongoCollection<OcrolusAccount> =
        anudeepAnanthDatabase.getCollection(Collections.OCROLUS_ACCOUNTS)

    val ocrolusAssignmentRunsCollection: MongoCollection<OcrolusAssignmentRun> =
        anudeepAnanthDatabase.getCollection(Collections.OCROLUS_ASSIGNMENT_RUNS)

    val ocrolusModelCallLogsCollection: MongoCollection<OcrolusModelCallLog> =
        anudeepAnanthDatabase.getCollection(Collections.OCROLUS_MODEL_CALL_LOGS)
}
