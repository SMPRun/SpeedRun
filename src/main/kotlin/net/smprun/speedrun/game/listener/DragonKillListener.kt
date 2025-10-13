package net.smprun.speedrun.game.listener

import net.smprun.speedrun.Speedrun
import net.smprun.common.annotations.AutoRegister
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import net.smprun.speedrun.events.DragonKillEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@AutoRegister
class DragonKillListener(private val plugin: Speedrun) : Listener {

    // Track the last player to damage the dragon
    private val lastDamager = ConcurrentHashMap<UUID, UUID>() // dragon UUID -> player UUID

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        if (entity !is EnderDragon) return
        
        // Find the player who caused the damage
        val damager = when (val d = event.damager) {
            is Player -> d
            is org.bukkit.entity.Projectile -> d.shooter as? Player
            else -> null
        }
        
        if (damager != null) {
            lastDamager[entity.uniqueId] = damager.uniqueId
        }
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        if (entity is EnderDragon) {
            // Get the killer from last damager tracking
            val killer = findLastDamager(entity)
            
            plugin.logger.info("Dragon killed! Detected killer: ${killer?.name ?: "none"}")
            handleDragonKill(killer)
            
            // Clean up the tracking map
            lastDamager.remove(entity.uniqueId)
        }
    }
    
    @EventHandler
    fun onCustomDragonKill(event: DragonKillEvent) {
        plugin.logger.info("Dragon kill event fired! Killer: ${event.killer?.name ?: "unknown"}")
        handleDragonKill(event.killer)
    }
    
    private fun findLastDamager(dragon: EnderDragon): Player? {
        val playerUuid = lastDamager[dragon.uniqueId] ?: return null
        return plugin.server.getPlayer(playerUuid)
    }
    
    private fun handleDragonKill(killer: Player?) {
        plugin.gameService.endGame(killer)
    }
}