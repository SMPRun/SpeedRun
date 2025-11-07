package net.smprun.speedrun.player.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import net.smprun.common.database.MongoService
import net.smprun.speedrun.player.PlayerStats
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

class PlayerStatsRepository(private val plugin: JavaPlugin) {

    private val collection: MongoCollection<PlayerStats> by lazy {
        val db = MongoService.database
            ?: throw IllegalStateException("Mongo database not initialized")
        db.getCollection("player_stats", PlayerStats::class.java)
    }

    suspend fun findByUuid(uuid: UUID): PlayerStats? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("uuid", uuid)).firstOrNull()
    }

    suspend fun upsert(stats: PlayerStats) = withContext(Dispatchers.IO) {
        val filter = Filters.eq("uuid", stats.uuid)
        val options = ReplaceOptions().upsert(true)
        collection.replaceOne(filter, stats, options)
    }
}

