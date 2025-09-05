package red.dan5.advancementsRTA

import io.papermc.paper.advancement.AdvancementDisplay
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import java.util.logging.Logger

class AdvancementListener(private val logger: Logger, private val tracker: AdvancementTracker) : Listener {
    
    @EventHandler
    fun onPlayerAdvancementDone(event: PlayerAdvancementDoneEvent) {
        val player = event.player
        val advancement = event.advancement
        val key = advancement.key.toString()
        
        // Check if player is first and skip if not tracked
        val queryResult = tracker.grantAdvancementAndCheckIsFirst(advancement, player)
        if (queryResult.isEmpty) {
            // Not a tracked advancement (likely a recipe)
            return
        }
        
        val isFirst = queryResult.get()
        logger.info("${player.name} completed advancement: $key (first: $isFirst)")
        
        // At this point, advancement.display is guaranteed to be non-null
        val advancementType = when (advancement.display!!.frame()) {
            AdvancementDisplay.Frame.TASK -> "進捗"
            AdvancementDisplay.Frame.GOAL -> "目標"
            AdvancementDisplay.Frame.CHALLENGE -> "挑戦"
        }
        
        // Build message with special formatting for first achievements
        val message = if (isFirst) {
            Component.text()
                .append(Component.text("■ 一番乗り！ ", NamedTextColor.GOLD)
                    .decorate(TextDecoration.BOLD))
                .append(Component.text("${player.name}が${advancementType}", NamedTextColor.GREEN))
                .append(advancement.displayName())
                .append(Component.text("を達成しました", NamedTextColor.GREEN))
                .decorate(TextDecoration.UNDERLINED)
                .build()
        } else {
            Component.text()
                .append(Component.text("${player.name}が${advancementType}", NamedTextColor.GREEN))
                .append(advancement.displayName())
                .append(Component.text("を達成しました", NamedTextColor.GREEN))
                .build()
        }
        
        // Send message and play sound for first achievements
        player.server.onlinePlayers.forEach { onlinePlayer ->
            onlinePlayer.sendMessage(message)
            if (isFirst) {
                onlinePlayer.playSound(
                    Sound.sound(
                        Key.key("minecraft:block.note_block.bell"),
                        Sound.Source.MASTER,
                        1.0f,
                        2.0f
                    )
                )
            }
        }
    }
}
