package net.smprun.speedrun.events

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class DragonKillEvent(val killer: Player?) : Event() {
    
    companion object {
        private val HANDLERS = HandlerList()
        
        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
    
    override fun getHandlers(): HandlerList = HANDLERS
}