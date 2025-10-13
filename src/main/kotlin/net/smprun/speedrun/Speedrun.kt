package net.smprun.speedrun

import co.aikar.commands.PaperCommandManager
import com.tcoded.folialib.FoliaLib
import net.smprun.common.CommonServices
import net.smprun.speedrun.game.GameService
import net.smprun.common.database.MongoService
import net.smprun.common.utils.RegistrationManager
import net.smprun.speedrun.scoreboard.ScoreboardService
import org.bukkit.plugin.java.JavaPlugin

class Speedrun : JavaPlugin() {

    lateinit var foliaLib: FoliaLib
    lateinit var commandManager: PaperCommandManager
    private lateinit var registrationManager: RegistrationManager
    lateinit var mongoService: MongoService
    lateinit var gameService: GameService
    lateinit var scoreboardService: ScoreboardService

    override fun onEnable() {
        foliaLib = FoliaLib(this)
        commandManager = PaperCommandManager(this)
        registrationManager = RegistrationManager(this, commandManager, basePackage = "net.smprun.speedrun")
        saveDefaultConfig()

        mongoService = CommonServices.mongo

        gameService = GameService(this)
        registrationManager.registerAll()

        scoreboardService = ScoreboardService(this)
        scoreboardService.start()
        
        logger.info("Speedrun plugin enabled!")
    }

    override fun onDisable() {
        if (this::scoreboardService.isInitialized) {
            scoreboardService.close()
        }
        logger.info("Speedrun plugin disabled!")
    }
}