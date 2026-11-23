package com.anudeepananth.projects.ocrolus.data

import com.anudeepananth.projects.ocrolus.OcrolusAccount
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.OcrolusModelCallLog
import com.anudeepananth.projects.ocrolus.OcrolusRunResult
import com.anudeepananth.projects.ocrolus.OcrolusRunStatus
import com.anudeepananth.projects.util.mongoDB.DatabaseReferences
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Projections
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory

private const val MONGO_DOCUMENT_ID_FIELD_NAME = "_id"

/**
 * Where runs, accounts and model-call records are saved. The pipeline talks to this interface
 * so tests can swap in an in-memory version; the real one below uses MongoDB.
 */
interface OcrolusRunStorage {
    suspend fun fetchAllAccounts(): List<OcrolusAccount>
    suspend fun saveRun(run: OcrolusAssignmentRun)
    suspend fun fetchRunById(runId: String): OcrolusAssignmentRun?
    suspend fun fetchAllRunsNewestFirst(): List<OcrolusAssignmentRun>
    suspend fun fetchRunListFieldsNewestFirst(): List<OcrolusAssignmentRun>
    suspend fun fetchFeaturedRunId(): String?
    suspend fun countFreeModelRunsStartedSince(sinceEpochMilliseconds: Long): Int
    suspend fun updateRunEditableFields(
        runId: String,
        displayLabel: String?,
        presenterNotes: String?,
        isFeatured: Boolean?
    ): Boolean

    suspend fun deleteRunAndItsModelCallLogs(runId: String): Boolean
    suspend fun markInterruptedRunsAsFailed(errorMessage: String)
    suspend fun saveModelCallLog(modelCallLog: OcrolusModelCallLog)
    suspend fun fetchModelCallLogsForRun(runId: String): List<OcrolusModelCallLog>
}

/** MongoDB-backed storage in the `Anudeep-Ananth` database (`ocrolus-*` collections). */
object OcrolusMongoRunStorage : OcrolusRunStorage {
    private val logger = LoggerFactory.getLogger("OcrolusMongoRunStorage")

    /** Creates the indexes the list and detail pages rely on. Safe to call on every server start. */
    suspend fun createIndexesInMongo() {
        DatabaseReferences.ocrolusAssignmentRunsCollection.createIndex(
            Indexes.descending(OcrolusAssignmentRun::startedAtEpochMilliseconds.name),
        )
        DatabaseReferences.ocrolusAssignmentRunsCollection.createIndex(
            Indexes.ascending(OcrolusAssignmentRun::status.name),
        )
        DatabaseReferences.ocrolusModelCallLogsCollection.createIndex(
            Indexes.ascending(OcrolusModelCallLog::runId.name, OcrolusModelCallLog::createdAtEpochMilliseconds.name),
            IndexOptions(),
        )
    }

    override suspend fun fetchAllAccounts(): List<OcrolusAccount> =
        DatabaseReferences.ocrolusAccountsCollection.find().sort(Sorts.ascending(MONGO_DOCUMENT_ID_FIELD_NAME)).toList()

    override suspend fun saveRun(run: OcrolusAssignmentRun) {
        DatabaseReferences.ocrolusAssignmentRunsCollection.replaceOne(
            Filters.eq(MONGO_DOCUMENT_ID_FIELD_NAME, run.runId),
            run,
            ReplaceOptions().upsert(true),
        )
    }

    override suspend fun fetchRunById(runId: String): OcrolusAssignmentRun? =
        DatabaseReferences.ocrolusAssignmentRunsCollection.find(Filters.eq(MONGO_DOCUMENT_ID_FIELD_NAME, runId))
            .firstOrNull()

    override suspend fun fetchAllRunsNewestFirst(): List<OcrolusAssignmentRun> =
        DatabaseReferences.ocrolusAssignmentRunsCollection
            .find()
            .sort(Sorts.descending(OcrolusAssignmentRun::startedAtEpochMilliseconds.name))
            .toList()

    /**
     * Same as [fetchAllRunsNewestFirst] but reads only the fields the list page shows, so it stays fast
     * however many runs exist. Everything else on the returned runs is left at its default.
     */
    override suspend fun fetchRunListFieldsNewestFirst(): List<OcrolusAssignmentRun> =
        DatabaseReferences.ocrolusAssignmentRunsCollection
            .find()
            .projection(
                Projections.include(
                    OcrolusAssignmentRun::assignmentName.name,
                    OcrolusAssignmentRun::status.name,
                    OcrolusAssignmentRun::modelId.name,
                    OcrolusAssignmentRun::modelDisplayName.name,
                    OcrolusAssignmentRun::isFreeModel.name,
                    OcrolusAssignmentRun::startedAtEpochMilliseconds.name,
                    OcrolusAssignmentRun::completedAtEpochMilliseconds.name,
                    OcrolusAssignmentRun::durationMilliseconds.name,
                    OcrolusAssignmentRun::isFeatured.name,
                    OcrolusAssignmentRun::displayLabel.name,
                    "${OcrolusAssignmentRun::result.name}.${OcrolusRunResult::chosenProduct.name}",
                ),
            )
            .sort(Sorts.descending(OcrolusAssignmentRun::startedAtEpochMilliseconds.name))
            .toList()

    override suspend fun fetchFeaturedRunId(): String? =
        DatabaseReferences.ocrolusAssignmentRunsCollection
            .withDocumentClass<org.bson.Document>()
            .find(Filters.eq(OcrolusAssignmentRun::isFeatured.name, true))
            .projection(Projections.include(MONGO_DOCUMENT_ID_FIELD_NAME))
            .firstOrNull()
            ?.getString(MONGO_DOCUMENT_ID_FIELD_NAME)

    override suspend fun countFreeModelRunsStartedSince(sinceEpochMilliseconds: Long): Int =
        DatabaseReferences.ocrolusAssignmentRunsCollection.countDocuments(
            Filters.and(
                Filters.eq(OcrolusAssignmentRun::isFreeModel.name, true),
                Filters.gte(OcrolusAssignmentRun::startedAtEpochMilliseconds.name, sinceEpochMilliseconds),
            ),
        ).toInt()

    override suspend fun updateRunEditableFields(
        runId: String,
        displayLabel: String?,
        presenterNotes: String?,
        isFeatured: Boolean?,
    ): Boolean {
        val fieldUpdates = listOfNotNull(
            displayLabel?.let {
                Updates.set(OcrolusAssignmentRun::displayLabel.name, it)
            },
            presenterNotes?.let {
                Updates.set(OcrolusAssignmentRun::presenterNotes.name, it)
            },
            isFeatured?.let {
                Updates.set(OcrolusAssignmentRun::isFeatured.name, it)
            },
        )
        if (fieldUpdates.isEmpty()) return fetchRunById(runId) != null
        if (isFeatured == true) {
            DatabaseReferences.ocrolusAssignmentRunsCollection.updateMany(
                Filters.eq(OcrolusAssignmentRun::isFeatured.name, true),
                Updates.set(OcrolusAssignmentRun::isFeatured.name, false),
            )
        }
        val updateResult = DatabaseReferences.ocrolusAssignmentRunsCollection.updateOne(
            Filters.eq(MONGO_DOCUMENT_ID_FIELD_NAME, runId),
            Updates.combine(fieldUpdates),
        )
        return updateResult.matchedCount > 0
    }

    override suspend fun deleteRunAndItsModelCallLogs(runId: String): Boolean {
        DatabaseReferences.ocrolusModelCallLogsCollection.deleteMany(Filters.eq(OcrolusModelCallLog::runId.name, runId))
        val deleteResult = DatabaseReferences.ocrolusAssignmentRunsCollection.deleteOne(
            Filters.eq(
                MONGO_DOCUMENT_ID_FIELD_NAME,
                runId
            )
        )
        return deleteResult.deletedCount > 0
    }

    override suspend fun markInterruptedRunsAsFailed(errorMessage: String) {
        DatabaseReferences.ocrolusAssignmentRunsCollection.updateMany(
            Filters.`in`(
                OcrolusAssignmentRun::status.name,
                OcrolusRunStatus.RUNNING.name,
                OcrolusRunStatus.QUEUED.name
            ),
            Updates.combine(
                Updates.set(OcrolusAssignmentRun::status.name, OcrolusRunStatus.FAILED.name),
                Updates.set(OcrolusAssignmentRun::errorMessage.name, errorMessage),
            ),
        )
    }

    override suspend fun saveModelCallLog(modelCallLog: OcrolusModelCallLog) {
        try {
            DatabaseReferences.ocrolusModelCallLogsCollection.insertOne(modelCallLog)
        } catch (exception: Exception) {
            logger.error("Could not save model call log for run ${modelCallLog.runId}: ${exception.message}", exception)
        }
    }

    override suspend fun fetchModelCallLogsForRun(runId: String): List<OcrolusModelCallLog> =
        DatabaseReferences.ocrolusModelCallLogsCollection
            .find(Filters.eq(OcrolusModelCallLog::runId.name, runId))
            .sort(Sorts.ascending(OcrolusModelCallLog::createdAtEpochMilliseconds.name))
            .toList()
}
