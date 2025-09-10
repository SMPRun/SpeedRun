package net.smprun.speedrun.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class GameStartEvent(val startTime: Long) : Event() {
    
    companion object {
        private val HANDLERS = HandlerList()
        
        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
    
    override fun getHandlers(): HandlerList = HANDLERS
}