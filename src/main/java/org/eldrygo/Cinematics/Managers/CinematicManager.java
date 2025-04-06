package org.eldrygo.Cinematics.Managers;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.eldrygo.Cinematics.CinematicSequence;
import org.eldrygo.Managers.StunManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;

import java.util.List;

public class CinematicManager {
    private final SpeedrunBoss plugin;
    private final ChatUtils chatUtils;
    private final StunManager stunManager;
    private final CountdownBossBarManager countdownBossBarManager;

    public CinematicManager(SpeedrunBoss plugin, ChatUtils chatUtils, StunManager stunManager, CountdownBossBarManager countdownBossBarManager) {
        this.plugin = plugin;
        this.chatUtils = chatUtils;
        this.stunManager = stunManager;
        this.countdownBossBarManager = countdownBossBarManager;
    }

    public void startCinematic() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            CinematicSequence sequence = new CinematicSequence(player, plugin)
                    .addAction(p -> countdownBossBarManager.startCountdown(60)) // Empieza el countdown con el mismo tiempo
                    .addDelay(980)  // Ajusta el delay dependiendo de tu flujo
                    .addAction(p -> stunManager.stunAllPlayers())
                    .addDelay(20)
                    .addAction(p -> p.sendTitle(
                            chatUtils.getTitle("cinematics.start.titles.countdown.10.title", player), null,
                            2, 16, 2))
                    .addAction(p -> p.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 1.5f))
                    .addDelay(20)
                    .addAction(p -> p.sendTitle(
                            chatUtils.getTitle("cinematics.start.titles.countdown.9.title", player), null,
                            2, 16, 2))
                    .addAction(p -> p.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 1.5f))
                    .addDelay(20)
                    .addAction(p -> p.sendTitle(
                            chatUtils.getTitle("cinematics.start.titles.countdown.8.title", player), null,
                            2, 16, 2))
                    .addAction(p -> p.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 1.5f))
                    .addDelay(20)
                    .addAction(p -> p.sendTitle(
                            chatUtils.getTitle("cinematics.start.titles.countdown.7.title", player), null,
                            2, 16, 2))
                    .addAction(p -> p.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 1.5f))
                    .addDelay(20)
                    .addAction(p -> p.sendTitle(
                            chatUtils.getTitle("cinematics.start.titles.countdown.6.title", player), null,
                            2, 16, 2))
                    .addAction(p -> p.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 1.5f))
                    .addDelay(20)
                    .addAction(p -> p.sendTitle(
                            chatUtils.getTitle("cinematics.start.titles.countdown.5.title", player), null,
                            2, 16, 2))
                    .addAction(p -> p.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 1.5f))
                    .addDelay(20)
                    .addAction(p -> p.sendTitle(
                            chatUtils.getTitle("cinematics.start.titles.countdown.4.title", player), null,
                            2, 16, 2))
                    .addAction(p -> p.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 1.5f))
                    .addDelay(20)
                    .addAction(p -> p.sendTitle(
                            chatUtils.getTitle("cinematics.start.titles.countdown.3.title", player), null,
                            2, 16, 2))
                    .addAction(p -> p.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 1.5f))
                    .addDelay(20)
                    .addAction(p -> p.sendTitle(
                            chatUtils.getTitle("cinematics.start.titles.countdown.2.title", player), null,
                            2, 16, 2))
                    .addAction(p -> p.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 1.5f))
                    .addDelay(20)
                    .addAction(p -> p.sendTitle(
                            chatUtils.getTitle("cinematics.start.titles.countdown.1.title", player), null,
                            2, 16, 2))
                    .addAction(p -> p.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 2f))
                    .addDelay(40)
                    .addAction(p -> p.sendTitle(
                            chatUtils.getTitle("cinematics.start.go.title", player), chatUtils.getSubtitle("cinematics.start.go.subtitle", player),
                            2, 60, 10))
                    .addAction(p -> p.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 10f, 2f))
                    .addAction(p -> p.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 10f, 1.5f))
                    .addAction(p -> p.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10f, 1.5f))
                    .addAction(p -> sendStartCinematicGoString(player))
                    .addAction(p -> stunManager.unStunAllPlayers())
            ;
            // Iniciar la secuencia
            sequence.start();
        }
    }
    private void sendStartCinematicGoString(Player receiver) {
        List<String> startGo = chatUtils.getMessageConfig().getStringList("cinematics.start.go.string");
        if (startGo.isEmpty()) {
            receiver.sendMessage(chatUtils.getMessage("error.string_not_found", receiver)
                    .replace("%path%", "cinematics.start.go.string"));
        }
        for (String line : startGo) {
            String formattedLine = PlaceholderAPI.setPlaceholders((OfflinePlayer) receiver, line);
            receiver.sendMessage(ChatUtils.formatColor(formattedLine));
        }
    }
}
