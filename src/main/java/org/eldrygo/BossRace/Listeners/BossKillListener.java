package org.eldrygo.BossRace.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.SpeedrunBoss;

public class BossKillListener implements Listener {
    private final BossKillManager bossKillManager;

    // Constructor que recibe el BossKillManager
    public BossKillListener(SpeedrunBoss plugin, BossKillManager bossKillManager) {
        this.bossKillManager = bossKillManager;
    }

    // Evento que escucha cuando una entidad muere
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Verificar si la muerte fue causada por un jugador
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            Entity entity = event.getEntity();

            // Comprobar si la entidad muerta es uno de los bosses relevantes
            if (isBoss(entity)) {
                // Llamar al método del BossKillManager para registrar la muerte
                bossKillManager.onBossKilled(entity, player);
            }
        }
    }

    // Método para verificar si la entidad es un boss
    private boolean isBoss(Entity entity) {
        return entity.getType() == EntityType.ENDER_DRAGON ||
                entity.getType() == EntityType.WITHER ||
                entity.getType() == EntityType.ELDER_GUARDIAN ||
                entity.getType() == EntityType.WARDEN;
    }
}