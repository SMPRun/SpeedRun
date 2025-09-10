package net.smprun.speedrun

import co.aikar.commands.PaperCommandManager
import com.tcoded.folialib.FoliaLib
import net.smprun.speedrun.utils.MongoService
import net.smprun.speedrun.utils.RegistrationManager
import org.bukkit.plugin.java.JavaPlugin

class Speedrun : JavaPlugin() {

    lateinit var foliaLib: FoliaLib
    lateinit var commandManager: PaperCommandManager
    private lateinit var registrationManager: RegistrationManager
    lateinit var mongoService: MongoService

    override fun onEnable() {
        foliaLib = FoliaLib(this)
        commandManager = PaperCommandManager(this)
        registrationManager = RegistrationManager(this)
        saveDefaultConfig()
        
        // Initialize MongoDB
        mongoService = MongoService(this)
        try {
            mongoService.connect()
        } catch (_: Exception) {
            logger.severe("MongoDB connection failed. Disabling plugin.")
            server.pluginManager.disablePlugin(this)
            return
        }
        
        registrationManager.registerAll()
        
        logger.info("Speedrun plugin enabled!")
    }

    override fun onDisable() {
        if (this::mongoService.isInitialized) {
            mongoService.close()
        }
        logger.info("Speedrun plugin disabled!")
    }
}
