package org.eldrygo.Spawn.Managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.Utils.LocationUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class SpawnManager {

    private final File file;
    private final XTeamsAPI xTeamsAPI;
    private final ChatUtils chatUtils;

    public SpawnManager(SpeedrunBoss plugin, XTeamsAPI xTeamsAPI, ChatUtils chatUtils) {
        this.file = new File(plugin.getDataFolder(), "data/spawns.json");
        this.xTeamsAPI = xTeamsAPI;
        this.chatUtils = chatUtils;

        if (!file.exists()) {
            saveData(new JSONObject());
        }
    }

    public void setGlobalSpawn(Location location) {
        JSONObject data = loadData();
        data.put("global", LocationUtils.serializeLocation(location));
        saveData(data);
    }

    public Location getGlobalSpawn() {
        JSONObject data = loadData();
        return LocationUtils.deserializeLocation((String) data.get("global"));
    }

    public void setTeamSpawn(String teamName, Location location) {
        JSONObject data = loadData();
        JSONObject teams = (JSONObject) data.getOrDefault("teams", new JSONObject());
        teams.put(teamName, LocationUtils.serializeLocation(location));
        data.put("teams", teams);
        saveData(data);
    }

    public Location getTeamSpawn(String teamName) {
        JSONObject data = loadData();
        JSONObject teams = (JSONObject) data.getOrDefault("teams", new JSONObject());
        String serialized = (String) teams.get(teamName);
        return serialized == null ? null : LocationUtils.deserializeLocation(serialized);
    }

    public boolean teleportTeam(String teamName) {
        Location location = getTeamSpawn(teamName);
        if (location == null) {
            Bukkit.getLogger().warning("[SpawnManager] No spawn found for team: " + teamName);
            return false;
        }

        Set<String> members = xTeamsAPI.getTeamMembers(teamName); // nicknames
        for (String nickname : members) {
            Player player = Bukkit.getPlayerExact(nickname);
            if (player != null && player.isOnline()) {
                player.teleport(location);
                player.sendMessage(ChatUtils.formatColor(chatUtils.getMessage("spawn.teleport.team", null)));
            }
        }
        return false;
    }

    public void teleportAllToGlobalSpawn() {
        Location location = getGlobalSpawn();
        if (location == null) {
            Bukkit.getLogger().warning("[SpawnManager] No global spawn defined.");
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(location);
            player.sendMessage(ChatUtils.formatColor(chatUtils.getMessage("spawn.teleport.global", null)));
        }
    }

    private JSONObject loadData() {
        try (FileReader reader = new FileReader(file)) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    private void saveData(JSONObject data) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(data.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
