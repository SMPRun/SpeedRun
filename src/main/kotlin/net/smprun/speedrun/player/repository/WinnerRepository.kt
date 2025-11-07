package net.smprun.speedrun.player.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import net.smprun.common.database.MongoService
import net.smprun.speedrun.player.WinnerRecord
import org.bukkit.plugin.java.JavaPlugin

class WinnerRepository(private val plugin: JavaPlugin) {

    private val collection: MongoCollection<WinnerRecord> by lazy {
        val db = MongoService.database
            ?: throw IllegalStateException("Mongo database not initialized")
        db.getCollection("winners", WinnerRecord::class.java)
    }

    suspend fun insert(record: WinnerRecord) = withContext(Dispatchers.IO) {
        collection.insertOne(record)
    }

    suspend fun listAll(): List<WinnerRecord> = withContext(Dispatchers.IO) {
        collection.find(Filters.empty())
            .sort(Sorts.ascending("winTime"))
            .toList()
    }

    suspend fun getBestTimeWinner(): WinnerRecord? = withContext(Dispatchers.IO) {
        collection.find(Filters.empty())
            .sort(Sorts.ascending("winTime"))
            .limit(1)
            .toList()
            .firstOrNull()
    }
}

