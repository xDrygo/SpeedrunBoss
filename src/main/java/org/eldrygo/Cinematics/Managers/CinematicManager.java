package org.eldrygo.Cinematics.Managers;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.eldrygo.Cinematics.Models.CinematicSequence;
import org.eldrygo.Cinematics.Models.CinematicType;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.Managers.StunManager;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.Spawn.Managers.SpawnManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Time.Managers.TimeBarManager;
import org.eldrygo.Time.Managers.TimeManager;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.Utils.OtherUtils;
import org.eldrygo.Utils.PlayerUtils;
import org.eldrygo.XTeams.Models.Team;

import java.util.*;

public class CinematicManager {
    private final SpeedrunBoss plugin;
    private final ChatUtils chatUtils;
    private final StunManager stunManager;
    private final CountdownBossBarManager countdownBossBarManager;
    private final GracePeriodManager gracePeriodManager;
    private final PVPManager pvpManager;
    private final PlayerUtils playerUtils;
    private final TimeManager timeManager;
    private final TimeBarManager timeBarManager;

    private final Map<CinematicType, CinematicSequence> cinematicTemplates = new HashMap<>();
    private CinematicSequence runningSequence;
    private final FireworkManager fireworkManager;
    private final OtherUtils otherUtils;
    public EventManager eventManager;
    private final SpawnManager spawnManager;

    public CinematicManager(SpeedrunBoss plugin, ChatUtils chatUtils, StunManager stunManager,
                            CountdownBossBarManager countdownBossBarManager, GracePeriodManager gracePeriodManager,
                            PVPManager pvpManager, PlayerUtils playerUtils,
                            TimeManager timeManager, TimeBarManager timeBarManager, FireworkManager fireworkManager, OtherUtils otherUtils, EventManager eventManager, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.chatUtils = chatUtils;
        this.stunManager = stunManager;
        this.countdownBossBarManager = countdownBossBarManager;
        this.gracePeriodManager = gracePeriodManager;
        this.pvpManager = pvpManager;
        this.playerUtils = playerUtils;
        this.timeManager = timeManager;
        this.timeBarManager = timeBarManager;
        this.fireworkManager = fireworkManager;
        this.otherUtils = otherUtils;
        this.eventManager = eventManager;
        this.spawnManager = spawnManager;

        cinematicTemplates.put(CinematicType.START, null); // Puedes registrar templates estáticos aquí
    }

    public void startCinematic(String cinematicName) {
        if (runningSequence != null && runningSequence.isRunning()) {
            plugin.getLogger().warning("A cinematic is already running.");
            return;
        }

        List<Player> players = playerUtils.getCinematicsPlayers();
        if (players.isEmpty()) return;

        CinematicType type = CinematicType.fromName(cinematicName);
        if (type == null) {
            plugin.getLogger().warning("Cinematic '" + cinematicName + "' not found.");
            return;
        }

        CinematicSequence sequence = createCinematicSequence(type, players);
        if (sequence != null) {
            runningSequence = sequence;
            sequence.start();
        }
    }

    public CinematicSequence getSequence(String name) {
        CinematicType type = CinematicType.fromName(name);
        if (type == null || runningSequence == null) return null;
        return runningSequence.getType() == type ? runningSequence : null;
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

    private CinematicSequence createCinematicSequence(CinematicType type, List<Player> players) {
        CinematicSequence sequence = new CinematicSequence(players, plugin, type);

        switch (type) {
            case START -> {
                sequence.addDelay(1);
                for (int i = 60; i >= 1; i--) {
                    final int num = i;
                    sequence
                            .thenGlobal(countdownBossBarManager::updateVoidBossBar)
                            .thenGlobal(() -> countdownBossBarManager.updateBossBar(60, num))
                            .then(p -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, num < 11 ? 10f : 0f, 1.5f))
                            .addDelay(20);
                }

                sequence
                        .then(p -> countdownBossBarManager.bossBar.setVisible(false))
                        .then(p -> countdownBossBarManager.voidBossBar.setVisible(false))
                        .then(p -> stunManager.stunAllPlayers())
                        .addDelay(20);
                for (Team t : plugin.getTeamManager().getAllTeams()) {
                    sequence.thenGlobal(() -> spawnManager.teleportTeam(t.getName()));
                }
                sequence.addDelay(100);

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

                sequence.then(p -> p.sendTitle(
                                chatUtils.getTitle("cinematics.start.go.title", p),
                                chatUtils.getSubtitle("cinematics.start.go.subtitle", p),
                                2, 60, 10))
                        .then(p -> {
                            p.playSound(p.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 10f, 2f);
                            p.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 10f, 1.5f);
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10f, 1.5f);
                        })
                        .then(this::sendStartCinematicGoString)
                        .thenGlobal(stunManager::unStunAllPlayers)
                        .thenGlobal(gracePeriodManager::startGracePeriod)
                        .thenGlobal(pvpManager::startPvPWithDelay)
                        .thenGlobal(timeManager::start)
                        .thenGlobal(timeBarManager::startUpdating)
                        .thenGlobal(timeBarManager::showAllBars);
            }
            case WINNER -> {
                sequence.thenGlobal(() -> eventManager.endEvent(null))
                        .then(otherUtils::giveInvisibility)
                        .addDelay(240)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .then(p -> p.playSound(p.getLocation(), Sound.BLOCK_VAULT_OPEN_SHUTTER, 10f, 1.5f))
                        .addDelay(10)
                        .then(p -> p.sendTitle(
                                chatUtils.getTitle("cinematics.winner.titles.winner.title", p).replace("%winner_team%", otherUtils.getWinnerTeamDisplay()),
                                chatUtils.getTitle("cinematics.winner.titles.winner.subtitle", p).replace("%winner_members%", otherUtils.getWinnerTeamMembers()),
                                10, 200, 30))
                        .then(p -> p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 10f, 1.5f))
                        .addDelay(10)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(20)
                        .thenGlobal(this::throwWinnerFireworks)
                        .addDelay(10)
                        .then(playerUtils::resetPlayerState)
                        .thenGlobal(playerUtils::tpPlayersToLobby)
                        .addDelay(20)
                        .thenGlobal(otherUtils::giveTrophy);
            }
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

    private void throwWinnerFireworks() {
        for (Player target : playerUtils.getWinnerTeamMembers()) {
            fireworkManager.spawnFireworksRing(target, 9 , 2);
        }
    }

    public List<String> getCinematicList() {
        List<String> list = new ArrayList<>();
        for (CinematicType type : CinematicType.values()) {
            list.add(type.getId());
        }
        return list;
    }
}
