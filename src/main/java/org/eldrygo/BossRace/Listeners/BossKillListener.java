package org.eldrygo.BossRace.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BossKillListener implements Listener {

    private final BossKillManager bossKillManager;
    private final XTeamsAPI xTeamsAPI;
    private double checkRadius = 30.0; // Radio por defecto
    private final ChatUtils chatUtils;
    private final EventManager eventManager;
    private final SpeedrunBoss plugin;

    public BossKillListener(BossKillManager bossKillManager, XTeamsAPI xTeamsAPI, ChatUtils chatUtils, EventManager eventManager, SpeedrunBoss plugin) {
        this.bossKillManager = bossKillManager;
        this.xTeamsAPI = xTeamsAPI;
        this.chatUtils = chatUtils;
        this.eventManager = eventManager;
        this.plugin = plugin;
    }

    // Setter para configurar el radio desde otra clase
    public void setCheckRadius(double radius) {
        this.checkRadius = radius;
    }
    private final Map<Location, Player> playerBedExplosionMap = new HashMap<>();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player killer)) return;

        Entity entity = event.getEntity();

        if (!isBoss(entity)) return;

        String team = xTeamsAPI.getPlayerTeamName(killer);
        if (team == null) return;

        bossKillManager.onBossKilled(entity, killer);
    }

    private boolean isBoss(Entity entity) {
        return entity.getType() == EntityType.ENDER_DRAGON ||
                entity.getType() == EntityType.WITHER ||
                entity.getType() == EntityType.ELDER_GUARDIAN ||
                entity.getType() == EntityType.WARDEN;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        if (!isBoss(entity)) return;
        double finalHealth = entity.getHealth() - event.getFinalDamage();

        if (finalHealth > 0) return; // No va a morir, así que ignoramos


        if (event instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

            if (damager instanceof Player) {
                cancelDeath(event, entity, ((Player) damager).getPlayer());
            } else if (damager instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof Player) {
                    cancelDeath(event, entity, ((Player) projectile.getShooter()).getPlayer());
                }
            }
        }
    }

    private void cancelDeath(EntityDamageEvent event, LivingEntity entity, Player killer) {
        String team = xTeamsAPI.getPlayerTeamName(killer);

        if (team == null) return;

        Set<String> teamMembers = xTeamsAPI.getTeamMembers(team);
        Location killerLocation = killer.getLocation();

        if (eventManager.getState() != EventManager.EventState.RUNNING) {
            killer.playSound(killerLocation, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 10f, 0.1f);
            killer.sendMessage(chatUtils.getMessage("warning.event_ended", null));
            event.setCancelled(true);
            return;
        }

        boolean allNearby = teamMembers.stream()
                .filter(name -> !name.equalsIgnoreCase(killer.getName())) // Excluye al killer
                .allMatch(name -> {
                    Player p = Bukkit.getPlayer(name);
                    if (p == null || !p.isOnline()) return false; // Si no está online, no está cerca
                    return p.getWorld().equals(killerLocation.getWorld()) &&
                            p.getLocation().distanceSquared(killerLocation) <= checkRadius * checkRadius;
                });

        if (!allNearby) {
            entity.setHealth(1.0);
            event.setCancelled(true);
            killer.playSound(killerLocation, Sound.BLOCK_ANVIL_LAND, 10f, 0.1f);
            killer.sendMessage(chatUtils.getMessage("warning.try_to_kill_boss_without_team_nearby.message", killer));
            killer.sendTitle(chatUtils.getTitle("warning.try_to_kill_boss_without_team_nearby.title", killer), chatUtils.getSubtitle("warning.try_to_kill_boss_without_team_nearby.subtitle", killer), 5, 40, 10);
        }
    }
}
