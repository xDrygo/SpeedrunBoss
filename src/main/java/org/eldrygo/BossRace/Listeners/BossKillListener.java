package org.eldrygo.BossRace.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.EntityType;
import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.XTeams.API.XTeamsAPI;

import java.util.Set;

public class BossKillListener implements Listener {

    private final BossKillManager bossKillManager;
    private final XTeamsAPI xTeamsAPI;
    private double checkRadius = 30.0; // Radio por defecto

    public BossKillListener(BossKillManager bossKillManager, XTeamsAPI xTeamsAPI) {
        this.bossKillManager = bossKillManager;
        this.xTeamsAPI = xTeamsAPI;
    }

    // Setter para configurar el radio desde otra clase
    public void setCheckRadius(double radius) {
        this.checkRadius = radius;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player killer)) return;

        Entity entity = event.getEntity();

        if (!isBoss(entity)) return;

        String team = xTeamsAPI.getPlayerTeamName(killer);
        if (team == null) return;

        Set<String> teamMembers = xTeamsAPI.getTeamMembers(team);
        Location bossLocation = entity.getLocation();

        boolean allNearby = teamMembers.stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .allMatch(p -> p.getWorld().equals(bossLocation.getWorld())
                        && p.getLocation().distanceSquared(bossLocation) <= checkRadius * checkRadius);

        if (!allNearby) {
            killer.sendMessage("§cNo todos los miembros de tu equipo están cerca del boss.");
            return;
        }

        bossKillManager.onBossKilled(entity, killer);
    }

    private boolean isBoss(Entity entity) {
        return entity.getType() == EntityType.ENDER_DRAGON ||
                entity.getType() == EntityType.WITHER ||
                entity.getType() == EntityType.ELDER_GUARDIAN ||
                entity.getType() == EntityType.WARDEN;
    }
}
