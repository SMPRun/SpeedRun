package net.smprun.speedrun

import co.aikar.commands.PaperCommandManager
import com.tcoded.folialib.FoliaLib
import gg.scala.flavor.Flavor
import gg.scala.flavor.FlavorOptions
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary
import net.smprun.common.Common
import net.smprun.common.utils.RegistrationManager
import net.smprun.speedrun.scoreboard.SpeedrunScoreboard
import org.bukkit.plugin.java.JavaPlugin

class Speedrun : JavaPlugin() {

    private lateinit var flavor: Flavor
    lateinit var commandManager: PaperCommandManager
    private lateinit var registrationManager: RegistrationManager
    lateinit var scoreboardService: SpeedrunScoreboard
    lateinit var foliaLib: FoliaLib
        private set

    override fun onEnable() {
        saveDefaultConfig()
        
        val common = Common.instance
        foliaLib = common.foliaLib
        
        val options = FlavorOptions(logger = logger)
        flavor = Flavor.create<Speedrun>(options)
        
        flavor.bind<JavaPlugin>().to(this)
        flavor.bind<Speedrun>().to(this)
        flavor.bind<FoliaLib>().to(foliaLib)
        common.scoreboardLibrary?.let {
            flavor.bind<ScoreboardLibrary>().to(it)
        }
        
        // Start Flavor (discovers and initializes all @Service objects)
        flavor.startup()
        
        commandManager = PaperCommandManager(this)
        registrationManager = RegistrationManager(this, commandManager, basePackage = "net.smprun.speedrun")
        registrationManager.registerAll()

        scoreboardService = SpeedrunScoreboard(
            this,
            foliaLib,
            common.scoreboardLibrary
        )
        common.registerScoreboard(scoreboardService)
        
        logger.info("Speedrun plugin enabled with Flavor DI!")
    }

    override fun onDisable() {
        try {
            flavor.close()
        } catch (_: Exception) {}
        
        logger.info("Speedrun plugin disabled!")
    }
}
