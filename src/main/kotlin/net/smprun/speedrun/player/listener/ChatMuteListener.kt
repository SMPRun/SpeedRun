package net.smprun.speedrun.player.listener

import net.smprun.common.annotations.AutoRegister
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

@AutoRegister
class ChatMuteListener() : Listener {

    private val bypassPermission = "speedrun.chat.bypass"

    @EventHandler(ignoreCancelled = true)
    fun onAsyncChat(event: AsyncChatEvent) {
        val player = event.player
        if (player.hasPermission(bypassPermission)) return
        event.isCancelled = true
    }
}