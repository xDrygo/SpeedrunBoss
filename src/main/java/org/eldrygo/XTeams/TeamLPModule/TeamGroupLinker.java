package org.eldrygo.XTeams.TeamLPModule;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.XTeams.Managers.ConfigManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TeamGroupLinker {

    private final SpeedrunBoss plugin;
    private final LuckPerms luckPerms;
    private final Map<String, String> teamGroupMap;

    public TeamGroupLinker(SpeedrunBoss plugin) {
        this.plugin = plugin;
        this.luckPerms = LuckPermsProvider.get();
        this.teamGroupMap = loadTeamGroupConfig();
    }
    public Map<String, String> loadTeamGroupConfig() {
        FileConfiguration config = getTeamGroupConfig();
        Map<String, String> map = new HashMap<>();

        ConfigurationSection section = config.getConfigurationSection("team-groups");
        if (section != null) {
            for (String team : section.getKeys(false)) {
                map.put(team.toLowerCase(), section.getString(team));
            }
        }

        return map;
    }
    public FileConfiguration getTeamGroupConfig() {
        File file = new File(plugin.getDataFolder(), "team_groups.yml");

        if (!file.exists()) {
            plugin.saveResource("team_groups.yml", false);
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    // Apply the group based on the team name
    public void applyGroup(Player player, String teamName) {
        String group = teamGroupMap.get(teamName.toLowerCase());
        plugin.getLogger().info(ChatUtils.formatColor("&eRemoving player " + player.getName() + " from the Luckperms group of " + teamName + "..."));

        if (group == null) {
            plugin.getLogger().warning(ChatUtils.formatColor("&c❌ No LuckPerms group found for team: " + teamName));
            return; // If no group is found, exit the method
        }

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            user.data().add(InheritanceNode.builder(group).build());
            luckPerms.getUserManager().saveUser(user);
        } else {
            plugin.getLogger().warning(ChatUtils.formatColor("&c❌ User not found for player: " + player.getName()));
        }
    }

    // Remove the group based on the team name
    public void removeGroup(Player player, String teamName) {
        String group = teamGroupMap.get(teamName.toLowerCase());

        plugin.getLogger().info(ChatUtils.formatColor("&eAdding player " + player.getName() + " to the Luckperms group of " + teamName + "..."));

        if (group == null) {
            plugin.getLogger().warning(ChatUtils.formatColor("&c❌ No LuckPerms group found for team: " + teamName));
            return; // If no group is found, exit the method
        }

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            user.data().remove(InheritanceNode.builder(group).build());
            luckPerms.getUserManager().saveUser(user);
        } else {
            plugin.getLogger().warning(ChatUtils.formatColor("&c❌ User not found for player: " + player.getName()));
        }
    }
}
