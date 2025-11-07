package net.smprun.speedrun.game

import com.tcoded.folialib.FoliaLib
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.smprun.common.utils.Colors
import net.smprun.common.utils.Text
import net.smprun.common.utils.TimeUtil
import net.smprun.speedrun.events.GameEndEvent
import net.smprun.speedrun.events.GameStartEvent
import net.smprun.speedrun.game.world.WorldService
import net.smprun.speedrun.player.PlayerStats
import net.smprun.speedrun.player.WinnerRecord
import net.smprun.speedrun.player.repository.PlayerStatsRepository
import net.smprun.speedrun.player.repository.WinnerRepository
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

@Service(name = "GameService", priority = 7)
object GameService {
    
    @Inject
    lateinit var plugin: JavaPlugin
    
    @Inject
    lateinit var foliaLib: FoliaLib
    
    private val winnerRepository by lazy { WinnerRepository(plugin) }
    private val playerStatsRepository by lazy { PlayerStatsRepository(plugin) }
    private val resetService by lazy { WorldService(plugin, foliaLib) }
    private val ioScope = CoroutineScope(Dispatchers.IO)
    
    @Volatile
    private var gameStartTime: Long? = null
    
    @Volatile
    var isGameActive: Boolean = false
        private set

    @Volatile
    private var resetScheduled: Boolean = false

    val isResetScheduled: Boolean
        get() = resetScheduled
    
    fun startGame() {
        if (isGameActive) {
            return
        }
        
        gameStartTime = System.currentTimeMillis()
        isGameActive = true
        resetScheduled = false
        
        plugin.logger.info("Speedrun game started at $gameStartTime")
        
        // Fire game start event
        val event = GameStartEvent(gameStartTime!!)
        Bukkit.getPluginManager().callEvent(event)
        
        // Broadcast start message
        Text.broadcast("Speedrun started! Race to defeat the Ender Dragon!", Colors.SUCCESS)
    }
    
    fun endGame(winner: Player?) {
        if (!isGameActive) {
            plugin.logger.info("Game end called but no game is active")
            return
        }
        
        plugin.logger.info("Ending game with winner: ${winner?.name ?: "none"}")
        
        val gameDuration = getGameDuration()
        if (gameDuration == null) {
            plugin.logger.warning("Cannot end game - no start time recorded")
            return
        }
        
        var winnerRecord: WinnerRecord? = null
        
        // Record winner if present
        if (winner != null) {
            winnerRecord = WinnerRecord(
                uuid = winner.uniqueId,
                username = winner.name,
                winTime = gameDuration
            )
            
            ioScope.launch {
                try {
                    // Save winner record
                    winnerRepository.insert(winnerRecord)
                    
                    // Update player stats
                    val stats = playerStatsRepository.findByUuid(winner.uniqueId)
                        ?: PlayerStats(uuid = winner.uniqueId)
                    playerStatsRepository.upsert(stats.addWin(gameDuration))
                } catch (e: Exception) {
                    plugin.logger.severe("Failed to record winner: ${e.message}")
                }
            }
            
            // Broadcast completion message
            val formattedTime = TimeUtil.formatTime(gameDuration)
            Text.broadcast("${winner.name} has defeated the Ender Dragon in $formattedTime!", Colors.BRAND_PRIMARY)
        }
        
        // Stop the game
        isGameActive = false
        plugin.logger.info("Speedrun game ended")
        
        // Fire game end event
        val event = GameEndEvent(winner, winnerRecord, gameDuration)
        Bukkit.getPluginManager().callEvent(event)
        
        // Schedule world reset
        scheduleWorldReset(winner, gameDuration)
    }
    
    fun forceStopGame() {
        if (!isGameActive) {
            return
        }
        
        isGameActive = false
        plugin.logger.info("Speedrun game force stopped")
        
        // Broadcast force stop message
        Text.broadcast("Speedrun has been force stopped by admin! Server will reset in 10 seconds...", Colors.ERROR)
        
        // Schedule immediate world reset
        foliaLib.scheduler.runLater(Runnable {
            resetService.resetAllWorlds(
                kickReason = Component.text("Speedrun force stopped by admin. Restarting with new world...")
            )
        }, 20L * 10) // 10 seconds
    }
    
    fun getGameDuration(): Long? {
        return gameStartTime?.let { System.currentTimeMillis() - it }
    }
    
    private fun scheduleWorldReset(winner: Player?, gameDuration: Long) {
        if (resetScheduled) {
            return
        }
        
        resetScheduled = true
        Text.broadcast("Server will reset in 60 seconds...", Colors.WARNING)
        
        foliaLib.scheduler.runLater(Runnable {
            val winnerText = winner?.name ?: "Unknown Winner"
            val timeText = " in ${TimeUtil.formatTime(gameDuration)}"
            resetService.resetAllWorlds(
                kickReason = Component.text("Speedrun complete! Winner: $winnerText$timeText. Restarting for a new run...")
            )
        }, 20L * 60) // 60 seconds
    }
}