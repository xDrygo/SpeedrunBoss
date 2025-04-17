package org.eldrygo.Modifiers.Managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Time.Managers.TimeManager;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.Utils.PlayerUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;

public class GracePeriodManager {
    private int gracePeriodDuration = 600; // ticks por defecto (30 segundos)
    private boolean isGracePeriodActive;
    private final SpeedrunBoss plugin;
    private final XTeamsAPI xTeamsAPI;
    public BukkitRunnable gracePeriodTask;
    private boolean gracePeriodPaused;
    private long remainingTime;
    private long startTime;
    private BroadcastManager broadcastManager;
    private final ChatUtils chatUtils;
    public PlayerUtils playerUtils;
    private final TimeManager timeManager;

    public GracePeriodManager(SpeedrunBoss plugin, XTeamsAPI xTeamsAPI, BroadcastManager broadcastManager, ChatUtils chatUtils, PlayerUtils playerUtils, TimeManager timeManager) {
        this.plugin = plugin;
        this.xTeamsAPI = xTeamsAPI;
        this.chatUtils = chatUtils;
        this.broadcastManager = broadcastManager;
        this.playerUtils = playerUtils;
        this.timeManager = timeManager;
        this.isGracePeriodActive = false;
        this.gracePeriodPaused = false;
        this.remainingTime = gracePeriodDuration;
    }

    public void startGracePeriod() {
        if (isGracePeriodActive || gracePeriodPaused) return;

        isGracePeriodActive = true;
        startTime = System.currentTimeMillis();
        remainingTime = gracePeriodDuration;

        // Aplicar efectos a todos los jugadores
        applyGracePeriodEffects();

        // Iniciar un temporizador para terminar el periodo de gracia
        gracePeriodTask = new BukkitRunnable() {
            @Override
            public void run() {
                stopGracePeriod();
            }
        };
        gracePeriodTask.runTaskLater(plugin, remainingTime);
    }

    public void stopGracePeriod() {
        if (!isGracePeriodActive) return;

        // Cancelar la tarea
        if (gracePeriodTask != null) {
            gracePeriodTask.cancel();
        }

        // Quitar efectos
        removeGracePeriodEffects();

        isGracePeriodActive = false;

        if (broadcastManager == null) {
            broadcastManager = new BroadcastManager(chatUtils, xTeamsAPI, playerUtils, timeManager);
            plugin.getLogger().warning("⚠ broadcastManager is null. Initializing a new BroadcastManager...");
        }

        broadcastManager.sendGracePeriodEndMessage();
    }

    private void applyGracePeriodEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String vaultGroup = playerUtils.getPrimaryGroup(player);
            String teamName = xTeamsAPI.getPlayerTeamName(player);

            boolean isInTeam = teamName != null && !teamName.isEmpty();
            boolean isNotAdminGroup = vaultGroup != null && !vaultGroup.contains("team_");

            if (isInTeam && isNotAdminGroup) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, gracePeriodDuration, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, gracePeriodDuration, 1));
            }
        }
    }

    private void removeGracePeriodEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.REGENERATION);
        }
    }

    public void setGracePeriodDuration(int ticks) {
        this.gracePeriodDuration = ticks;
        this.remainingTime = ticks;
    }

    public boolean isGracePeriodActive() {
        return isGracePeriodActive;
    }

    public boolean isGracePeriodPaused() {
        return gracePeriodPaused;
    }

    public void pauseGracePeriod() {
        if (!isGracePeriodActive || gracePeriodPaused) return;

        gracePeriodPaused = true;
        if (gracePeriodTask != null) {
            gracePeriodTask.cancel();
        }

        long elapsed = (System.currentTimeMillis() - startTime) / 50L;
        remainingTime = Math.max(1, gracePeriodDuration - elapsed);
    }

    public void resumeGracePeriod() {
        if (!gracePeriodPaused) return;

        gracePeriodPaused = false;
        startTime = System.currentTimeMillis();

        gracePeriodTask = new BukkitRunnable() {
            @Override
            public void run() {
                stopGracePeriod();
            }
        };
        gracePeriodTask.runTaskLater(plugin, remainingTime);
    }
}
