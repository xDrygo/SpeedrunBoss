package org.eldrygo.Modifiers.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.eldrygo.Modifiers.Managers.PVPManager;

public class PvpListener implements Listener {
    private final PVPManager pvpManager;

    public PvpListener(PVPManager pvpManager) {
        this.pvpManager = pvpManager;
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        // Verificamos que la entidad que recibe el daño sea un jugador
        if (!(event.getEntity() instanceof Player victim)) return;

        Entity damager = event.getDamager();

        // Daño directo cuerpo a cuerpo
        if (damager instanceof Player attacker) {
            if (!pvpManager.isPvpEnabled()) {
                event.setCancelled(true);
            }
        }

        // Daño por proyectiles (flechas, bolas de nieve, etc.)
        else if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                if (!pvpManager.isPvpEnabled()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}