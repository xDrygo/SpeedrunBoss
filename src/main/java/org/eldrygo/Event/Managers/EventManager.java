package org.eldrygo.Event.Managers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.ModifierManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.Utils.DepUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventManager {
    private final ChatUtils chatUtils;
    private final DepUtils depUtils;
    private final CinematicManager cinematicManager;
    private final XTeamsAPI xTeamsAPI;

    public enum EventState { NOT_STARTED, RUNNING, PAUSED, ENDED }
    private EventState state;
    private final Set<UUID> participants;
    private final EventDataManager eventDataManager;
    private final GracePeriodManager gracePeriodManager;
    private final PVPManager pvpManager;

    public EventManager(ChatUtils chatUtils, XTeamsAPI xTeamsAPI, DepUtils depUtils, CinematicManager cinematicManager, BroadcastManager broadcastManager, EventDataManager eventDataManager, ModifierManager modifierManager, SpeedrunBoss plugin) {
        this.chatUtils = chatUtils;
        this.xTeamsAPI = xTeamsAPI;
        this.depUtils = depUtils;
        this.cinematicManager = cinematicManager;
        this.eventDataManager = eventDataManager;
        this.state = EventState.NOT_STARTED;
        this.participants = new HashSet<>();
        this.gracePeriodManager = new GracePeriodManager(plugin, xTeamsAPI, broadcastManager, chatUtils);
        this.pvpManager = new PVPManager(10 * 20L, plugin, broadcastManager, chatUtils, xTeamsAPI);
    }

    public void startEvent(Player sender) {
        if (state == EventState.RUNNING) {
            sender.sendMessage(chatUtils.getMessage("administration.event.start.already_started", sender));
            return;
        }

        state = EventState.RUNNING;
        participants.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String vaultGroup = depUtils.getPrimaryGroup(player);
            String teamName = xTeamsAPI.getPlayerTeamName(player);
            if (teamName == null || teamName.isEmpty()) {
                continue;
            }
            if (player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            if (vaultGroup.equalsIgnoreCase("staff")) {
                continue;
            }

            participants.add(player.getUniqueId());
        }
        pvpManager.stopPvP();
        cinematicManager.startCinematic("start");

        eventDataManager.saveEventState(state, participants);
        sender.sendMessage(chatUtils.getMessage("administration.event.start.success", sender));
    }

    public void pauseEvent(Player sender) {
        if (state != EventState.RUNNING) return;

        state = EventState.PAUSED;
        eventDataManager.saveEventState(state, participants);
        sender.sendMessage(chatUtils.getMessage("administration.event.pause.success", sender));
    }

    public void resumeEvent(Player sender) {
        if (state != EventState.PAUSED) return;

        state = EventState.RUNNING;
        sender.sendMessage(chatUtils.getMessage("administration.event.resume.success", sender));
    }

    public void endEvent(Player sender) {
        if (state == EventState.ENDED) return;

        state = EventState.ENDED;
        eventDataManager.clearEventData();
        gracePeriodManager.stopGracePeriod();
        pvpManager.stopPvP();
        sender.sendMessage(chatUtils.getMessage("administration.event.end.success", sender));
    }

    public EventState getState() {
        return state;
    }

    public Set<UUID> getParticipants() {
        return participants;
    }
}
