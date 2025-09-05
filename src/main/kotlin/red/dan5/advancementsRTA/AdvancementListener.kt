package red.dan5.advancementsRTA

import io.papermc.paper.advancement.AdvancementDisplay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import java.util.logging.Logger

class AdvancementListener(private val logger: Logger) : Listener {
    
    @EventHandler
    fun onPlayerAdvancementDone(event: PlayerAdvancementDoneEvent) {
        val player = event.player
        val advancement = event.advancement
        val key = advancement.key.toString()
        
        // Skip recipe advancements
        if (key.startsWith("minecraft:recipes/")) {
            return
        }
        
        logger.info("${player.name} completed advancement: $key")
        
        // Get advancement type text based on frame
        val advancementType = when (advancement.display?.frame()) {
            AdvancementDisplay.Frame.TASK -> "進捗"
            AdvancementDisplay.Frame.GOAL -> "目標"
            AdvancementDisplay.Frame.CHALLENGE -> "挑戦"
            else -> "(フレームが存在しません)" // Default fallback
        }
        
        // Broadcast in Japanese
        val message = Component.text()
            .append(Component.text("${player.name}が${advancementType}", NamedTextColor.GREEN))
            .append(advancement.displayName())
            .append(Component.text("を達成しました！", NamedTextColor.GREEN))
            .build()
        
        player.server.onlinePlayers.forEach { onlinePlayer ->
            onlinePlayer.sendMessage(message)
        }
    }
}
