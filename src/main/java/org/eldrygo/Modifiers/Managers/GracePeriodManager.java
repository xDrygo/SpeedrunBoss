package org.eldrygo.Modifiers.Managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.DepUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;

import java.util.List;

public class GracePeriodManager {
    private int gracePeriodDuration = 20 * 60; // Duración del tiempo de gracia en ticks (ej. 20 ticks = 1 segundo)
    private boolean isGracePeriodActive;
    private final SpeedrunBoss plugin;
    private final DepUtils depUtils;
    private final XTeamsAPI xTeamsAPI;
    public BukkitRunnable gracePeriodTask;
    private boolean gracePeriodPaused;
    private long remainingTime;
    private long startTime;
    private final BroadcastManager broadcastManager;

    public GracePeriodManager(SpeedrunBoss plugin, XTeamsAPI XTeamsAPI, BroadcastManager broadcastManager) {
        this.broadcastManager = broadcastManager;
        this.isGracePeriodActive =  false;
        this.gracePeriodPaused = false;
        this.remainingTime = gracePeriodDuration;
        this.plugin = plugin;
        this.depUtils = new DepUtils();
        this.xTeamsAPI = XTeamsAPI;
        this.startTime = System.currentTimeMillis();
    }

    public void startGracePeriod() {
        if (isGracePeriodActive || gracePeriodPaused) return;

        isGracePeriodActive = true;

        // Aplicar efectos a todos los jugadores
        applyGracePeriodEffects();

        // Iniciar un temporizador para que el tiempo de gracia termine después de un periodo determinado
        gracePeriodTask = new BukkitRunnable() {
            @Override
            public void run() {
                stopGracePeriod(); // Detener el periodo de gracia al finalizar
            }
        };
        gracePeriodTask.runTaskLater(plugin, remainingTime);
    }
    public void stopGracePeriod() {
        if (!isGracePeriodActive) {
            return; // Si el periodo de gracia no está activo, no hacer nada
        }

        // Cancelar la tarea programada
        if (gracePeriodTask != null) {
            cancelTask(gracePeriodTask);
        }

        // Eliminar efectos de gracia
        removeGracePeriodEffects();

        // Cambiar el estado de la bandera
        isGracePeriodActive = false;

        broadcastManager.sendGracePeriodEndMessage();
    }
    public void pauseGracePeriod() {
        if (!isGracePeriodActive || gracePeriodPaused) return; // Si no está activo o ya está pausado, no hacer nada

        gracePeriodPaused = true;
        // Calculamos el tiempo transcurrido
        long elapsed = System.currentTimeMillis() - startTime;
        remainingTime -= elapsed; // Calculamos el tiempo restante al momento de pausar
        gracePeriodTask.cancel(); // Cancelamos el temporizador
    }

    public void resumeGracePeriod() {
        if (!gracePeriodPaused) return; // Si no está pausado, no hacer nada

        gracePeriodPaused = false;
        startTime = System.currentTimeMillis(); // Actualizamos la marca de tiempo al reanudar
        startGracePeriod(); // Reiniciamos el periodo de gracia
    }
    private void applyGracePeriodEffects() {
        List<Player> players = (List<Player>) Bukkit.getOnlinePlayers();
        for (Player player : players) {
            String vaultGroup = depUtils.getPrimaryGroup(player);
            String teamName = xTeamsAPI.getPlayerTeamName(player);
            if (!(vaultGroup.contains("team_")) && !(teamName == null) && !(teamName.isEmpty())) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, gracePeriodDuration, 1)); // Efecto de velocidad
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, gracePeriodDuration, 1)); // Efecto de regeneración
            }
        }
    }

    private void removeGracePeriodEffects() {
        List<Player> players = (List<Player>) Bukkit.getOnlinePlayers();
        for (Player player : players) {
            // Eliminar los efectos de poción
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.REGENERATION);
        }
    }
    private void cancelTask(BukkitRunnable task) {
        if (task != null) {
            task.cancel();
        }
    }
    public boolean isGracePeriodActive() {
        return isGracePeriodActive;
    }
    public boolean isGracePeriodPaused() {
        return gracePeriodPaused;
    }
}