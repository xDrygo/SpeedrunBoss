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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TeamGroupLinker {

    private final SpeedrunBoss plugin;
    private final LuckPerms luckPerms;
    private Map<String, String> teamGroupMap;

    public TeamGroupLinker(SpeedrunBoss plugin) {
        this.plugin = plugin;
        this.luckPerms = LuckPermsProvider.get();
        this.teamGroupMap = loadTeamGroupConfig();
    }

    // Load team to group mapping from config
    public Map<String, String> loadTeamGroupConfig() {
        FileConfiguration config = getTeamGroupConfig();
        Map<String, String> map = new HashMap<>();

        ConfigurationSection section = config.getConfigurationSection("team-groups");
        if (section != null) {
            for (String team : section.getKeys(false)) {
                String group = section.getString(team);
                if (group != null && !group.isBlank()) {
                    map.put(team.toLowerCase(), group);
                }
            }
        }
        return map;
    }
    public void reloadTeamGroupConfig() {
        this.teamGroupMap = loadTeamGroupConfig(); // Recargar el mapa de equipos y grupos
        plugin.getLogger().info(ChatUtils.formatColor("&ateam_groups.yml reloaded."));
    }

    // Load the YML config file
    public FileConfiguration getTeamGroupConfig() {
        File file = new File(plugin.getDataFolder(), "team_groups.yml");

        if (!file.exists()) {
            plugin.saveResource("team_groups.yml", false);
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    // Apply the group to the player based on team name
    public void applyGroup(Player player, String teamName) {
        handleGroupChange(player, teamName, true);
    }

    // Remove the group from the player based on team name
    public void removeGroup(Player player, String teamName) {
        handleGroupChange(player, teamName, false);
    }

    // Unified logic for adding/removing LuckPerms group
    private void handleGroupChange(Player player, String teamName, boolean add) {
        String teamKey = teamName.toLowerCase();
        String group = teamGroupMap.get(teamKey);

        if (group == null || group.isBlank()) {
            plugin.getLogger().warning(ChatUtils.formatColor("&c❌ No LuckPerms group found for team: " + teamName));
            return;
        }

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            plugin.getLogger().warning(ChatUtils.formatColor("&c❌ User not found for player: " + player.getName()));
            return;
        }

        InheritanceNode node = InheritanceNode.builder(group).build();

        if (add) {
            user.data().add(node);
        } else {
            user.data().remove(node);
        }

        luckPerms.getUserManager().saveUser(user);
    }
}
