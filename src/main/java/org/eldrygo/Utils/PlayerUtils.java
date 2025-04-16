package org.eldrygo.Utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.eldrygo.XTeams.TeamLPModule.TeamGroupLinker;

import java.util.*;

public class PlayerUtils {
    private final SettingsUtils settingsUtils;
    private final XTeamsAPI xTeamsAPI;
    private final TeamGroupLinker teamGroupLinker;

    public PlayerUtils(SettingsUtils settingsUtils, XTeamsAPI xTeamsAPI, TeamGroupLinker teamGroupLinker) {
        this.settingsUtils = settingsUtils;
        this.xTeamsAPI = xTeamsAPI;
        this.teamGroupLinker = teamGroupLinker;
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
                Bukkit.getLogger().info("The team" + teamName + " don't have members, skipping...");
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
                        Bukkit.getLogger().warning("Can't find the group " + group + " for the team " + teamName);
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
}
