package net.smprun.speedrun.utils

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.UuidRepresentation
import net.smprun.speedrun.Speedrun

class MongoService(private val plugin: Speedrun) {

    private var client: MongoClient? = null
    var database: MongoDatabase? = null
        private set

    fun connect() {
        val config = plugin.config
        val defaultUri = "mongodb://localhost:27017/speedrun"
        val uriFromEnv = System.getenv("MONGODB_URI")
        val uriFromConfig = config.getString("mongodb.uri") ?: defaultUri
        val databaseName = config.getString("mongodb.database") ?: "speedrun"

        val finalUri = uriFromEnv ?: uriFromConfig

        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(finalUri))
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .build()

        client = MongoClient.create(settings)
        database = client!!.getDatabase(databaseName)

        try {
            runBlocking {
                database!!.runCommand<Document>(Document("ping", 1))
            }
            plugin.logger.info("Connected to MongoDB at $finalUri (db=${databaseName})")
        } catch (ex: Exception) {
            plugin.logger.severe("Failed to connect to MongoDB: ${ex.message}")
            throw ex
        }
    }

    fun close() {
        try {
            client?.close()
        } catch (_: Exception) {
        } finally {
            client = null
            database = null
        }
    }
}


