package org.eldrygo.BossRace.Listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;

import java.util.*;

public class BedExplosionListener implements Listener {

    /*
    private final SpeedrunBoss plugin;
    private final EventManager eventManager;
    private final XTeamsAPI xTeamsAPI;
    private final ChatUtils chatUtils;
    private final double checkRadius;

    private final Map<Location, Player> bedClickMap = new HashMap<>();
    private final Map<Location, Player> recentExplosions = new HashMap<>();
    private final Map<UUID, Player> lastExplosionDamager = new HashMap<>();
    private final BossKillManager bossKillManager;

    public BedExplosionListener(SpeedrunBoss plugin, EventManager eventManager, XTeamsAPI xTeamsAPI, ChatUtils chatUtils, double checkRadius, BossKillManager bossKillManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
        this.xTeamsAPI = xTeamsAPI;
        this.chatUtils = chatUtils;
        this.checkRadius = checkRadius;
        this.bossKillManager = bossKillManager;
    }
    @EventHandler
    public void onBedUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || !block.getType().name().endsWith("_BED")) return;

        World.Environment env = block.getWorld().getEnvironment();
        if (env == World.Environment.NETHER || env == World.Environment.THE_END) {
            Location loc = block.getLocation();
            Player player = event.getPlayer();

            bedClickMap.put(loc, player);
            Bukkit.getScheduler().runTaskLater(plugin, () -> bedClickMap.remove(loc), 40L);
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        Location explosionLoc = event.getLocation();

        for (Map.Entry<Location, Player> entry : bedClickMap.entrySet()) {
            Location bedLoc = entry.getKey();
            if (bedLoc.getWorld().equals(explosionLoc.getWorld()) && bedLoc.distanceSquared(explosionLoc) <= 9) {
                Player responsible = entry.getValue();
                plugin.getLogger().info("Bed explosion triggered by " + responsible.getName());
                recentExplosions.put(explosionLoc, responsible);

                Bukkit.getScheduler().runTaskLater(plugin, () -> recentExplosions.remove(explosionLoc), 60L);
                break;
            }
        }
    }
    @EventHandler
    public void onExplosionDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        if (event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        Location entityLoc = entity.getLocation();
        Player closest = null;
        double closestDistance = 3.5;

        for (Map.Entry<Location, Player> entry : recentExplosions.entrySet()) {
            Location explosionLoc = entry.getKey();
            if (explosionLoc.getWorld().equals(entityLoc.getWorld())) {
                double dist = explosionLoc.distance(entityLoc);
                if (dist < closestDistance) {
                    closest = entry.getValue();
                    closestDistance = dist;
                }
            }
        }

        if (closest != null) {
            // ✔ Comprobación de miembros del equipo cerca
            String team = xTeamsAPI.getPlayerTeamName(closest);
            if (team != null) {
                Set<String> teamMembers = xTeamsAPI.getTeamMembers(team);

                if (eventManager.getState() != EventManager.EventState.RUNNING) {
                    closest.playSound(closest.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 10f, 0.1f);
                    closest.sendMessage(chatUtils.getMessage("warning.event_ended", null));
                    event.setCancelled(true);
                    return;
                }

                boolean allNearby = teamMembers.stream()
                        .filter(name -> !name.equalsIgnoreCase(closest.getName()))
                        .allMatch(name -> {
                            Player p = Bukkit.getPlayer(name);
                            if (p == null || !p.isOnline()) return false;
                            return p.getWorld().equals(closest.getWorld()) &&
                                    p.getLocation().distanceSquared(closest.getLocation()) <= checkRadius * checkRadius;
                        });

                if (!allNearby) {
                    entity.setHealth(1.0);
                    event.setCancelled(true);
                    closest.playSound(closest.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 10f, 1.75f);
                    closest.sendMessage(chatUtils.getMessage("warning.try_to_kill_boss_without_team_nearby.message", closest));
                    closest.sendTitle(
                            chatUtils.getTitle("warning.try_to_kill_boss_without_team_nearby.title", closest),
                            chatUtils.getSubtitle("warning.try_to_kill_boss_without_team_nearby.subtitle", closest),
                            5, 40, 10
                    );
                    return;
                }
            }

            // Guardar para la muerte si todo OK
            lastExplosionDamager.put(entity.getUniqueId(), closest);
            plugin.getLogger().info(entity.getName() + " damaged by bed explosion from " + closest.getName());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        UUID id = entity.getUniqueId();
        Player killer = lastExplosionDamager.remove(id);

        if (killer != null) {
            if (!isBoss(entity)) return;

            String team = xTeamsAPI.getPlayerTeamName(killer);
            if (team == null) return;

            bossKillManager.onBossKilled(entity, killer);
        }
    }

    private boolean isBoss(Entity entity) {
        return entity.getType() == EntityType.ENDER_DRAGON ||
                entity.getType() == EntityType.WITHER ||
                entity.getType() == EntityType.ELDER_GUARDIAN ||
                entity.getType() == EntityType.WARDEN;
    }
     */
}
