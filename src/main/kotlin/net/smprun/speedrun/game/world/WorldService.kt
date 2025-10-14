package net.smprun.speedrun.game.world

import net.kyori.adventure.text.Component
import net.smprun.common.CommonServices
import net.smprun.speedrun.Speedrun
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File
import kotlin.random.Random

class WorldService(private val plugin: Speedrun) {

    fun resetAllWorlds(kickReason: Component) {
        // Generate new random seed for the overworld
        val newSeed = Random.nextLong()

        plugin.logger.info("=== SPEEDRUN WORLD RESET ===")
        plugin.logger.info("Setting new world seed: $newSeed")
        plugin.logger.info("=============================")

        try {
            // Kick everyone first
            kickAllPlayers(kickReason)

            // Wait a moment for players to disconnect
            Thread.sleep(1000)

            // Clean up old world folders before setting new configuration
            cleanupOldWorlds()

            // Update server configuration with new seed and world settings
            updateServerConfiguration(newSeed)

            // Schedule server restart
            CommonServices.foliaLib.scheduler.runLater(Runnable {
                plugin.logger.info("Restarting server with new world seed: $newSeed")
                plugin.logger.info("Old worlds cleaned up and new ones will be generated on startup...")
                Bukkit.getServer().shutdown()
            }, 20L) // 1-second delay

        } catch (e: Exception) {
            plugin.logger.severe("Error during world reset: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun kickAllPlayers(reason: Component) {
        val players: List<Player> = ArrayList(Bukkit.getOnlinePlayers())
        players.forEach { player ->
            player.kick(reason)
        }
    }

    private fun cleanupOldWorlds() {
        try {
            // First, read the current world name from server.properties
            val currentWorldName = getCurrentWorldName()
            plugin.logger.info("Current world name: $currentWorldName")

            val serverDir = plugin.server.worldContainer
            // Delete all world folders except the current one
            val worldFolders = serverDir.listFiles { file ->
                file.isDirectory && (
                    file.name.startsWith("world_") ||
                    file.name == "world" ||
                    file.name == "world_nether" ||
                    file.name == "world_the_end"
                ) && file.name != currentWorldName &&
                file.name != "${currentWorldName}_nether" &&
                file.name != "${currentWorldName}_the_end"
            }

            var deletedCount = 0
            worldFolders?.forEach { folder ->
                try {
                    plugin.logger.info("Deleting old world folder: ${folder.name}")
                    deleteRecursively(folder)
                    if (!folder.exists()) {
                        deletedCount++
                        plugin.logger.info("Successfully deleted: ${folder.name}")
                    } else {
                        plugin.logger.warning("Failed to delete: ${folder.name}")
                    }
                } catch (e: Exception) {
                    plugin.logger.warning("Error deleting ${folder.name}: ${e.message}")
                }
            }

            if (deletedCount > 0) {
                plugin.logger.info("Cleaned up $deletedCount old world folders")
            } else {
                plugin.logger.info("No old world folders to clean up")
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error during world cleanup: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun getCurrentWorldName(): String {
        return try {
            val serverProps = File("server.properties")
            if (serverProps.exists()) {
                val content = serverProps.readText()
                val levelNameMatch = Regex("level-name=(.*)").find(content)
                levelNameMatch?.groupValues?.get(1)?.trim() ?: "world"
            } else {
                "world"
            }
        } catch (e: Exception) {
            plugin.logger.warning("Could not read current world name: ${e.message}")
            "world"
        }
    }

    private fun deleteRecursively(file: File) {
        if (!file.exists()) return

        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                deleteRecursively(child)
            }
        }

        // Try multiple times to delete stubborn files
        var attempts = 0
        while (file.exists() && attempts < 5) {
            try {
                val deleted = file.delete()
                if (!deleted && file.exists()) {
                    Thread.sleep(100)
                }
            } catch (_: Exception) {
                Thread.sleep(100)
            }
            attempts++
        }
    }

    private fun updateServerConfiguration(newSeed: Long) {
        try {
            val serverProps = File("server.properties")
            if (serverProps.exists()) {
                var content = serverProps.readText()

                // Update the level seed
                content = content.replace(Regex("level-seed=.*"), "level-seed=$newSeed")

                // Update level name to force new world generation
                val timestamp = System.currentTimeMillis()
                val newWorldName = "world_$timestamp"
                content = content.replace(Regex("level-name=.*"), "level-name=$newWorldName")

                serverProps.writeText(content)
                plugin.logger.info("Updated server.properties:")
                plugin.logger.info("  level-seed=$newSeed")
                plugin.logger.info("  level-name=$newWorldName")
            } else {
                plugin.logger.warning("server.properties not found!")
            }
        } catch (e: Exception) {
            plugin.logger.severe("Failed to update server.properties: ${e.message}")
            e.printStackTrace()
        }
    }
}