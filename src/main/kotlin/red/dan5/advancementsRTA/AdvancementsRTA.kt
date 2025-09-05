package red.dan5.advancementsRTA

import org.bukkit.plugin.java.JavaPlugin

class AdvancementsRTA : JavaPlugin() {
    private lateinit var gameRuleSetter: GameRuleSetter
    private lateinit var advancementListener: AdvancementListener

    override fun onEnable() {
        // Plugin startup logic

        gameRuleSetter = GameRuleSetter(server, logger)
        gameRuleSetter.set()
        
        // Register advancement listener
        advancementListener = AdvancementListener(logger)
        server.pluginManager.registerEvents(advancementListener, this)
    }

    override fun onDisable() {
        // Plugin shutdown logic

        gameRuleSetter.restore()
    }
}
