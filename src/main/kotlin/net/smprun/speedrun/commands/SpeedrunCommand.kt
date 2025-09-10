package net.smprun.speedrun.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.annotations.AutoRegister
import net.smprun.speedrun.game.WorldResetService
import net.smprun.speedrun.player.repository.PlayerRepository
import net.smprun.speedrun.winner.WinnerRecord
import net.smprun.speedrun.winner.repository.WinnerRepository
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.command.CommandSender

@AutoRegister
@CommandAlias("speedrun|sr")
@CommandPermission("speedrun.admin")
class SpeedrunCommand(private val plugin: Speedrun) : BaseCommand() {

    private val resetService = WorldResetService(plugin)
    private val winnerRepo = WinnerRepository(plugin)
    private val playerRepo = PlayerRepository(plugin)

    @Subcommand("complete")
    @Description("Complete the speedrun and reset worlds with new seeds")
    fun onComplete(sender: CommandSender) {
        val winnerName: String
        val winnerUuid: java.util.UUID?

        if (sender is Player) {
            winnerName = sender.name
            winnerUuid = sender.uniqueId
        } else {
            // fallback to first online player or console-named winner
            val p = Bukkit.getOnlinePlayers().firstOrNull()
            winnerName = p?.name ?: "Console"
            winnerUuid = p?.uniqueId
        }

        // Mock insert winner and update player stats asynchronously
        plugin.foliaLib.scheduler.runAsync { _ ->
            try {
                if (winnerUuid != null) {
                    val winTime = System.currentTimeMillis() // placeholder
                    val record = WinnerRecord(uuid = winnerUuid, username = winnerName, winTime = winTime)
                    kotlinx.coroutines.runBlocking { winnerRepo.insert(record) }

                    val player = kotlinx.coroutines.runBlocking { playerRepo.findByUuid(winnerUuid) }
                    if (player != null) {
                        kotlinx.coroutines.runBlocking { playerRepo.upsert(player.addWin(winTime)) }
                    }
                }
            } catch (_: Exception) {
            }
        }

        Bukkit.getServer().broadcast(Component.text("$winnerName has completed the speedrun! Server will reset in 60 seconds...", NamedTextColor.GOLD))

        plugin.foliaLib.scheduler.runLater(Runnable {
            resetService.resetAllWorlds(
                kickReason = Component.text("Speedrun complete! Winner: ${winnerName}. Restarting for a new run...", NamedTextColor.GREEN)
            )
        }, 20L * 60)
    }
}


