package red.dan5.advancementsRTA

import org.bukkit.plugin.java.JavaPlugin

class AdvancementsRTA : JavaPlugin() {
    private lateinit var gameRuleSetter: GameRuleSetter
    private lateinit var advancementListener: AdvancementListener
    private lateinit var advancementTracker: AdvancementTracker

    override fun onEnable() {
        // Plugin startup logic

        gameRuleSetter = GameRuleSetter(server, logger)
        gameRuleSetter.set()
        
        // Initialize advancement tracker
        advancementTracker = AdvancementTracker(logger, this)
        advancementTracker.initialize()
        
        // Register advancement listener
        advancementListener = AdvancementListener(logger)
        server.pluginManager.registerEvents(advancementListener, this)
    }

    override fun onDisable() {
        // Plugin shutdown logic

        gameRuleSetter.restore()
    }
}
