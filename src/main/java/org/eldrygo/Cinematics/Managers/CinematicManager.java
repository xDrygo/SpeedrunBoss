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
import org.eldrygo.Utils.PlayerUtils;
import org.eldrygo.Utils.SettingsUtils;

import java.util.ArrayList;
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

    private final Map<String, CinematicSequence> cinematicTemplates = new HashMap<>();
    private CinematicSequence runningSequence;
    private PlayerUtils playerUtils;

    public CinematicManager(SpeedrunBoss plugin, ChatUtils chatUtils, StunManager stunManager, CountdownBossBarManager countdownBossBarManager, GracePeriodManager gracePeriodManager, PVPManager pvpManager, PlayerUtils playerUtils) {
        this.plugin = plugin;
        this.chatUtils = chatUtils;
        this.stunManager = stunManager;
        this.countdownBossBarManager = countdownBossBarManager;
        this.gracePeriodManager = gracePeriodManager;
        this.pvpManager = pvpManager;
        this.playerUtils = playerUtils;

        // Registrar cinemáticas disponibles al iniciar
        cinematicTemplates.put("start", null); // se crea dinámicamente por jugador
    }

    public void startCinematic(String cinematicName) {
        if (runningSequence != null && runningSequence.isRunning()) {
            plugin.getLogger().warning("A cinematic is already running.");
            return;
        }

        List<Player> players = playerUtils.getCinematicsPlayers();
        if (players.isEmpty()) return;

        CinematicSequence sequence = createCinematicSequence(cinematicName, players);
        if (sequence != null) {
            runningSequence = sequence;
            sequence.start();
        }
    }

    public CinematicSequence getSequence(String name) {
        return "start".equalsIgnoreCase(name) ? runningSequence : null;
    }

    public CinematicSequence getRunningSequence() {
        return runningSequence != null && runningSequence.isRunning() ? runningSequence : null;
    }

    public void stopCinematic() {
        if (runningSequence != null && runningSequence.isRunning()) {
            runningSequence.stop();
            runningSequence = null;
        }
    }

    private CinematicSequence createCinematicSequence(String cinematicName, List<Player> players) {
        if (!"start".equalsIgnoreCase(cinematicName)) {
            plugin.getLogger().warning("Can't find cinematic: " + cinematicName);
            return null;
        }

        CinematicSequence sequence = new CinematicSequence(players, plugin, cinematicName);

        sequence.addDelay(1);
        for (int i = 60; i >= 1; i--) {
            final int num = i;
            sequence
                    .then(p -> countdownBossBarManager.updateVoidBossBar())
                    .then(p -> countdownBossBarManager.updateBossBar(60, num))
                    .then(p -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, num < 11 ? 10f : 0f, 1.5f))
                    .addDelay(20);
        }

        sequence
                .then(p -> countdownBossBarManager.bossBar.setVisible(false))
                .then(p -> countdownBossBarManager.voidBossBar.setVisible(false))
                .then(p -> stunManager.stunAllPlayers())
                .then(p -> p.sendMessage(ChatUtils.formatColor("&eAquí empieza el stun para hacer el TP...")))
                .addDelay(20)
                .then(p -> p.sendMessage(ChatUtils.formatColor("&eAquí sería el TP de los equipos...")))
                .addDelay(100);

        for (int i = 10; i >= 1; i--) {
            final int num = i;
            sequence
                    .then(p -> p.sendTitle(
                            chatUtils.getTitle("cinematics.start.titles.countdown." + num + ".title", p),
                            null,
                            2, 16, 2))
                    .then(p -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10f, num == 1 ? 2f : 1.5f))
                    .addDelay(20);
        }

        sequence
                .then(p -> p.sendTitle(
                        chatUtils.getTitle("cinematics.start.go.title", p),
                        chatUtils.getSubtitle("cinematics.start.go.subtitle", p),
                        2, 60, 10))
                .then(p -> {
                    p.playSound(p.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 10f, 2f);
                    p.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 10f, 1.5f);
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10f, 1.5f);
                })
                .then(this::sendStartCinematicGoString)
                .then(p -> p.sendMessage(ChatUtils.formatColor("&eInicia el periodo de gracia, inicia el delay para que se active el PVP, se quita el stun e inicia el evento...")))
                .then(p -> stunManager.unStunAllPlayers())
                .then(p -> gracePeriodManager.startGracePeriod())
                .then(p -> pvpManager.startPvPWithDelay());

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

    public List<String> getCinematicList() {
        List<String> list = new ArrayList<>(); // Inicializa la lista correctamente
        list.add("start");
        return list;
    }
}
