package net.smprun.speedrun.events

import net.smprun.speedrun.winner.WinnerRecord
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class GameEndEvent(
    val winner: Player?,
    val winnerRecord: WinnerRecord?,
    val gameDuration: Long
) : Event() {
    
    companion object {
        private val HANDLERS = HandlerList()
        
        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
    
    override fun getHandlers(): HandlerList = HANDLERS
}