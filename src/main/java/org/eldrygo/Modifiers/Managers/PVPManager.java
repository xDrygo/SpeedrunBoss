package org.eldrygo.Modifiers.Managers;

import org.bukkit.scheduler.BukkitRunnable;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.SpeedrunBoss;

public class PVPManager {
    private boolean pvpEnabled;
    private final long pvpStartDelay; // El delay en ticks
    private final SpeedrunBoss plugin;
    private final BroadcastManager broadcastManager;

    public PVPManager(long pvpStartDelay, SpeedrunBoss plugin, BroadcastManager broadcastManager) {
        this.plugin = plugin;
        this.broadcastManager = broadcastManager;
        this.pvpEnabled = false;
        this.pvpStartDelay = pvpStartDelay;
    }

    // Activar PvP con delay
    public void startPvPWithDelay() {
        if (pvpEnabled) return; // Si PvP ya está habilitado, no hacer nada

        // Aquí se ejecuta el código cuando se activa el PVP después del delay
        // Si tienes efectos específicos para el PVP, agrégalos aquí
        BukkitRunnable pvpTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Aquí se ejecuta el código cuando se activa el PVP después del delay
                pvpEnabled = true;
                //applyPvPEffects(); // Si tienes efectos específicos para el PVP, agrégalos aquí
                broadcastManager.sendPVPEnableMessage();
            }
        };

        // Ejecutar el task después del retraso
        pvpTask.runTaskLater(plugin, pvpStartDelay); // 'plugin' es la instancia de tu plugin y pvpStartDelay es el retraso en ticks
    }

    // Activar PvP inmediatamente (sin delay)
    public void forceStartPvP() {
        if (pvpEnabled) return; // Si PvP ya está habilitado, no hacer nada

        pvpEnabled = true;
        //applyPvPEffects(); // Si tienes efectos específicos para el PVP, agrégalos aquí
    }

    // Detener PvP
    public void stopPvP() {
        if (!pvpEnabled) return;

        pvpEnabled = false;
        //removePvPEffects(); // Si tienes efectos que quitar, agrégalo aquí
    }

    // Verificar si el PvP está habilitado
    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    /*
    // Método para aplicar efectos PvP (agrega tu lógica)
    private void applyPvPEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Aquí podrías aplicar efectos como daño aumentado, invencibilidad, etc.
            player.sendMessage("¡PVP activado!");
        }
    }

    // Método para remover efectos PvP (agrega tu lógica)
    private void removePvPEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Aquí podrías quitar efectos aplicados en PvP, como daño aumentado, etc.
            player.sendMessage("¡PVP desactivado!");
        }
    }*/
}
