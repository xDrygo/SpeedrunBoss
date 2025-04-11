package org.eldrygo.Cinematics.Managers;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.eldrygo.Cinematics.CinematicSequence;
import org.eldrygo.Managers.StunManager;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.Utils.SettingsUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CinematicManager {
    private final SpeedrunBoss plugin;
    private final ChatUtils chatUtils;
    private final StunManager stunManager;
    private final CountdownBossBarManager countdownBossBarManager;
    private final GracePeriodManager gracePeriodManager;
    private final PVPManager pvpManager;
    private final SettingsUtils settingsUtils;

    private final Map<String, CinematicSequence> activeCinematics = new HashMap<>();

    public CinematicManager(SpeedrunBoss plugin, ChatUtils chatUtils, StunManager stunManager, CountdownBossBarManager countdownBossBarManager, GracePeriodManager gracePeriodManager, PVPManager pvpManager, SettingsUtils settingsUtils) {
        this.plugin = plugin;
        this.chatUtils = chatUtils;
        this.stunManager = stunManager;
        this.countdownBossBarManager = countdownBossBarManager;
        this.gracePeriodManager = gracePeriodManager;
        this.pvpManager = pvpManager;
        this.settingsUtils = settingsUtils;
    }

    public void startCinematic(String cinematicName) {
        if (activeCinematics.containsKey(cinematicName)) {
            plugin.getLogger().warning("Cinematic " + cinematicName + " is already running.");
            return;
        }

        for (Player player : settingsUtils.getCinematicsPlayers()) {
            CinematicSequence sequence = createCinematicSequence(cinematicName, player);
            activeCinematics.put(cinematicName, sequence);
            sequence.start();
        }
    }

    private CinematicSequence createCinematicSequence(String cinematicName, Player player) {
        CinematicSequence sequence = new CinematicSequence(player, plugin, cinematicName);

        if ("start".equalsIgnoreCase(cinematicName)) {
            sequence.then(p -> countdownBossBarManager.startCountdown(60))
                    .addDelay(980)
                    .then(p -> stunManager.stunAllPlayers())
                    .then(p -> p.sendMessage(ChatUtils.formatColor("&eAquí empieza el stun para hacer el TP...")))

                    .addDelay(20)
                    .then(p -> p.sendMessage(ChatUtils.formatColor("&eAquí sería el TP de los equipos...")))

                    .addDelay(100);

            for (int i = 10; i >= 1; i--) {
                final int num = i;
                sequence.then(p -> p.sendTitle(
                                chatUtils.getTitle("cinematics.start.titles.countdown." + num + ".title", player),
                                null,
                                2, 16, 2))
                        .then(p -> p.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10f, num == 1 ? 2f : 1.5f))
                        .addDelay(20);
            }

            sequence.then(p -> p.sendTitle(
                            chatUtils.getTitle("cinematics.start.go.title", player),
                            chatUtils.getSubtitle("cinematics.start.go.subtitle", player),
                            2, 60, 10))
                    .then(p -> {
                        p.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 10f, 2f);
                        p.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 10f, 1.5f);
                        p.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10f, 1.5f);
                    })
                    .then(p -> sendStartCinematicGoString(player))
                    .then(p -> p.sendMessage(ChatUtils.formatColor("&eInicia el periodo de gracia, inicia el delay para que se active el PVP, se quita el stun e inicia el evento...")))
                    .then(p -> stunManager.unStunAllPlayers())
                    .then(p -> gracePeriodManager.startGracePeriod())
                    .then(p -> pvpManager.startPvPWithDelay());

        } else {
            plugin.getLogger().warning("Can't find cinematic: " + cinematicName);
        }

        return sequence;
    }

    private void sendStartCinematicGoString(Player receiver) {
        List<String> startGo = chatUtils.getMessageConfig().getStringList("cinematics.start.go.string");
        if (startGo.isEmpty()) {
            receiver.sendMessage(chatUtils.getMessage("error.string_not_found", receiver)
                    .replace("%path%", "cinematics.start.go.string"));
            return;
        }
        for (String line : startGo) {
            String formattedLine = PlaceholderAPI.setPlaceholders((OfflinePlayer) receiver, line);
            receiver.sendMessage(ChatUtils.formatColor(formattedLine));
        }
    }
}
