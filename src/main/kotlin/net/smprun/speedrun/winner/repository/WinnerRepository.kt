package net.smprun.speedrun.winner.repository

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.winner.WinnerRecord
import net.smprun.speedrun.winner.WinnersList

class WinnerRepository(private val plugin: Speedrun) {

    private val collection: MongoCollection<WinnerRecord> by lazy {
        val db = plugin.mongoService.database
            ?: throw IllegalStateException("Mongo database not initialized")
        db.getCollection("winners", WinnerRecord::class.java)
    }

    suspend fun insert(record: WinnerRecord) = withContext(Dispatchers.IO) {
        collection.insertOne(record)
    }

    suspend fun listAll(): WinnersList = withContext(Dispatchers.IO) {
        val all = collection.find(Filters.empty()).toList()
        WinnersList(all)
    }
}