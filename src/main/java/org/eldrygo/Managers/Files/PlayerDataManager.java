package org.eldrygo.Managers.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.eldrygo.SpeedrunBoss;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class PlayerDataManager {

    private final File dataFile;
    private final Gson gson;
    private final Map<String, String> playerData;

    public PlayerDataManager(SpeedrunBoss plugin) {
        this.dataFile = new File(plugin.getDataFolder(), "data/players.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.playerData = new HashMap<>();

        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
                try (FileWriter writer = new FileWriter(dataFile)) {
                    writer.write("{}"); // Inicializa el archivo con un objeto vacío
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            load();
        }
    }

    public void load() {
        if (!dataFile.exists()) {
            save(); // Crea el archivo vacío si no existe
            return;
        }

        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> data = gson.fromJson(reader, type);
            if (data != null) {
                playerData.clear();
                playerData.putAll(data);
            }
        } catch (IOException e) {
            Bukkit.getLogger().severe("[PlayerDataManager] Error loading players.json:");
            e.printStackTrace();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(playerData, writer);
        } catch (IOException e) {
            Bukkit.getLogger().severe("[PlayerDataManager] Error saving players.json:");
            e.printStackTrace();
        }
    }

    public void setStatus(String nickname, String status) {
        playerData.put(nickname, status);
    }

    public String getStatus(String nickname) {
        return playerData.getOrDefault(nickname, "noob"); // Por defecto noob si no existe
    }

    public List<String> getPlayersWithStatus(String status) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : playerData.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(status)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public boolean hasStatus(String nickname) {
        return playerData.containsKey(nickname);
    }

    public void removePlayer(String nickname) {
        playerData.remove(nickname);
    }

    public Set<String> getAllPlayers() {
        return playerData.keySet();
    }
}
