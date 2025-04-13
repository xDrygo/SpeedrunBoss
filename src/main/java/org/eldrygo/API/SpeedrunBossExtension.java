package org.eldrygo.API;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpeedrunBossExtension extends PlaceholderExpansion {
    private final SpeedrunBoss plugin;
    private final TeamDataManager teamDataManager;
    private final XTeamsAPI xTeamsAPI;
    private final ConfigManager configManager;

    public SpeedrunBossExtension(SpeedrunBoss plugin, TeamDataManager teamDataManager, XTeamsAPI xTeamsAPI, ConfigManager configManager) {
        this.plugin = plugin;
        this.teamDataManager = teamDataManager;
        this.xTeamsAPI = xTeamsAPI;
        this.configManager = configManager;
    }

    @Override
    public @NotNull String getIdentifier() { return "spb"; }

    @Override
    public @NotNull String getAuthor() { return "Drygo"; }

    @Override
    public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }

    public String onRequest(OfflinePlayer player, String params) {
        if (params.startsWith("tab_remainingbosses_")) { //%spb_tab_remainingbosses_<boss>%
            String bossName = params.replace("tab_remainingbosses_", "");
            String teamName = xTeamsAPI.getPlayerTeamName(player);

            switch (bossName) {
                case "warden" -> {
                    if (teamDataManager.hasKilledBoss(teamName, "warden")) {
                        return "ड";
                    } else {
                        return "ढ";
                    }
                }
                case "wither" -> {
                    if (teamDataManager.hasKilledBoss(teamName, "wither")) {
                        return "ण";
                    } else {
                        return "त";
                    }
                }
                case "elderguardian" -> {
                    if (teamDataManager.hasKilledBoss(teamName, "elder_guardian")) {
                        return "थ";
                    } else {
                        return "द";
                    }
                }
                case "enderdragon" -> {
                    if (teamDataManager.hasKilledBoss(teamName, "ender_dragon")) {
                        return "ध";
                    } else if (teamDataManager.hasKilledRequiredBosses(teamName)) {
                        return "न";
                    } else {
                        return "ऩ";
                    }
                }
            }
        }
        if (params.startsWith("tab_teammate")){
            return getTeammatesFormatted(player);
        }
        if (params.startsWith("tab_teamdisplayname")) {
            String output;
            if (xTeamsAPI.getPlayerTeamDisplayName(player) != "No Team") {
                output = "&r&f&lᴛᴜ ᴇqᴜɪᴘᴏ: " + xTeamsAPI.getPlayerTeamDisplayName(player);
            } else {
                output = ChatUtils.formatColor(configManager.getMessageString("placeholders.tablist.teammates.no_members"));
            }
            return output;
        }
        return null;
    }

    private String getTeammatesFormatted(OfflinePlayer player) {
        String teamName = xTeamsAPI.getPlayerTeamName(player);
        List<String> teamMembers = new ArrayList<>(xTeamsAPI.getTeamMembers(teamName));

        teamMembers.removeIf(member -> member.equalsIgnoreCase(player.getName()));

        String noMembersMsg = " ";
        String memberFormat = configManager.getMessageString("placeholders.tablist.teammates.members");
        String separator = configManager.getMessageString("placeholders.tablist.teammates.members_separator");

        String teamMembersFormatted = teamMembers.isEmpty() ? noMembersMsg :
                teamMembers.stream()
                        .map(member -> memberFormat.replace("%member_name%", member))
                        .collect(Collectors.joining(separator));

        return ChatUtils.formatColor(teamMembersFormatted);
    }
}
