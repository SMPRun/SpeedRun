package net.smprun.speedrun.player.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.player.Player
import java.util.UUID

class PlayerRepository(private val plugin: Speedrun) {

    private val collection: MongoCollection<Player> by lazy {
        val db = plugin.mongoService.database
            ?: throw IllegalStateException("Mongo database not initialized")
        db.getCollection("players", Player::class.java)
    }

    suspend fun findByUuid(uuid: UUID): Player? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("uuid", uuid)).firstOrNull()
    }

    suspend fun upsert(player: Player) = withContext(Dispatchers.IO) {
        val filter = Filters.eq("uuid", player.uuid)
        val options = ReplaceOptions().upsert(true)
        collection.replaceOne(filter, player, options)
    }
}