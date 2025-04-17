package org.eldrygo.Utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.eldrygo.XTeams.TeamLPModule.TeamGroupLinker;

import java.util.*;

public class PlayerUtils {
    private SettingsUtils settingsUtils;
    private final XTeamsAPI xTeamsAPI;
    private final TeamGroupLinker teamGroupLinker;
    private final SpeedrunBoss plugin;

    public PlayerUtils(SettingsUtils settingsUtils, XTeamsAPI xTeamsAPI, TeamGroupLinker teamGroupLinker, SpeedrunBoss plugin) {
        this.settingsUtils = settingsUtils;
        this.xTeamsAPI = xTeamsAPI;
        this.teamGroupLinker = teamGroupLinker;
        this.plugin = plugin;
    }

    public List<Player> getAdminPlayers() {
        List<Player> adminPlayers = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getPrimaryGroup(player).equals("staff") || getPrimaryGroup(player).equals("Jose90")) {
                adminPlayers.add(player);
            }
        }
        return adminPlayers;
    }
    public List<Player> getCinematicsPlayers() {
        List<Player> cinematicsPlayers = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (settingsUtils.cinematicBypassEnabled) {
                if (!settingsUtils.cinematicBypassPlayers.contains(String.valueOf(player))) {
                    cinematicsPlayers.add(player);
                }
            } else {
                cinematicsPlayers.add(player);
            }
        }
        return cinematicsPlayers;
    }


    public void tpPlayersToLobby() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            World lobbyWorld = Bukkit.getWorld("lobby");
            Location location = new Location(lobbyWorld, 0.5, 100.25, 0.5, 90f, 0f);
            player.teleport(location);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10f, 2f);
        }
    }

    public List<Player> getWinnerTeamMembers() {
        List<Player> winnerTeamMembers = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.winnerTeam != null) {
                String winnerTeamName = plugin.winnerTeam;
                String playerName = player.getName();
                if (xTeamsAPI.getTeamMembers(winnerTeamName).contains(playerName)) {
                    winnerTeamMembers.add(player);
                }
            }
        }
        return winnerTeamMembers;
    }

    public String getPrimaryGroup(Player player) {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return "";
        }
        Permission perms = rsp.getProvider();
        return perms.getPrimaryGroup(player);
    }

    public void updateTeamGroups() {
        Map<String, String> teamGroupMap = teamGroupLinker.loadTeamGroupConfig(); // Mapa equipo -> grupo
        LuckPerms luckPerms = LuckPermsProvider.get();

        for (String teamName : teamGroupMap.keySet()) {
            Set<String> members = xTeamsAPI.getTeamMembers(teamName);

            // Comprobar si el equipo tiene miembros
            if (members.isEmpty()) {
                plugin.getLogger().info("The team" + teamName + " don't have members, skipping...");
                continue; // Si no hay miembros, saltar a la siguiente iteración
            }

            String group = teamGroupMap.get(teamName);

            for (String memberName : members) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberName);
                if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) continue;

                UUID uuid = offlinePlayer.getUniqueId();
                luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
                    // Obtener los nodos de herencia (parent groups)
                    List<Node> toRemove = user.getNodes().stream()
                            .filter(node -> node instanceof InheritanceNode) // Solo nodos de herencia
                            .toList();

                    // Eliminar los nodos de herencia
                    toRemove.forEach(node -> user.data().remove(node));

                    // Agregar el nuevo grupo
                    Group groupObj = luckPerms.getGroupManager().getGroup(group);
                    if (groupObj != null) {
                        user.data().add(InheritanceNode.builder(group).build());
                        luckPerms.getUserManager().saveUser(user);
                    } else {
                        plugin.getLogger().warning("Can't find the group " + group + " for the team " + teamName);
                    }
                });
            }
        }
    }
    public void updatePlayerGroupBasedOnTeam(Player player) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        UserManager userManager = luckPerms.getUserManager();

        // Obtener el nombre del equipo
        String teamName = xTeamsAPI.getPlayerTeamName(player);
        if (teamName == null) return;

        // Obtener el grupo asignado a ese equipo desde la config
        String group = teamGroupLinker.loadTeamGroupConfig().get(teamName.toLowerCase());
        if (group == null) return;

        // Cargar el usuario de LuckPerms y hacer los cambios
        userManager.loadUser(player.getUniqueId()).thenAcceptAsync(user -> {
            // Limpiar grupos parent (herencia)
            user.data().clear(NodeType.INHERITANCE::matches);

            // Agregar el grupo correspondiente al equipo
            InheritanceNode node = InheritanceNode.builder(group).value(true).build();
            user.data().add(node);

            // Guardar el usuario con los cambios
            luckPerms.getUserManager().saveUser(user);
        });
    }

    public void setSettingsUtils(SettingsUtils settingsUtils) {
        this.settingsUtils = settingsUtils;
    }
    /*
    public String getPrefix(Player player) {
        String prefix = "";
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            CachedMetaData metaData = user.getCachedData().getMetaData();
            if (metaData.getPrefix() != null) {
                prefix = metaData.getPrefix();
            }
        }
        return ChatUtils.formatColor(prefix);
    }
     */
    public void resetPlayerState(Player player) {
        // Limpiar inventario
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setExtraContents(new ItemStack[1]);
        player.setItemOnCursor(null);

        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getDefaultValue());
        player.setFoodLevel(20);
        player.setSaturation(5f);

        player.setExp(0f);
        player.setLevel(0);
        player.setTotalExperience(0);

        player.setFireTicks(0);
        player.setFallDistance(0f);

        player.setArrowsInBody(0); // esto remueve las flechas visibles del cuerpo
    }
}
