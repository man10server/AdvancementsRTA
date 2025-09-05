package red.dan5.advancementsRTA

import org.bukkit.plugin.java.JavaPlugin

class AdvancementsRTA : JavaPlugin() {
    private lateinit var gameRuleSetter: GameRuleSetter

    override fun onEnable() {
        // Plugin startup logic

        gameRuleSetter = GameRuleSetter(server, logger)
        gameRuleSetter.set()
    }

    override fun onDisable() {
        // Plugin shutdown logic

        gameRuleSetter.restore()
    }
}
