package org.eldrygo.Managers.Files;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TeamDataManager {
    private final File dataFile;
    private final XTeamsAPI xTeamsAPI;

    public TeamDataManager(JavaPlugin plugin, XTeamsAPI xTeamsAPI) {
        this.dataFile = new File(plugin.getDataFolder(), "data/teams.json");
        this.xTeamsAPI = xTeamsAPI;

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
        }
    }

    public List<String> getKilledBosses(String teamName) {
        JSONObject json = readJsonFile();
        List<String> killedBosses = new ArrayList<>();

        if (json.containsKey(teamName)) { // Cambié "has" por "containsKey"
            JSONArray array = (JSONArray) json.get(teamName); // Cambié "optJSONArray" por "get"
            if (array != null) {
                for (Object obj : array) {
                    killedBosses.add(obj.toString()); // "optString" no existe en json-simple
                }
            }
        }
        return killedBosses;
    }
    public void addDimensionRegister(String teamName, String dimension) {
        JSONObject json = readJsonFile();
        JSONArray dimensions = (JSONArray) json.get(teamName); // Cambié "optJSONArray" por "get"

        if (dimensions == null) {
            dimensions = new JSONArray();
        }

        if (!dimensions.contains(dimension)) { // "toList" no existe en json-simple, pero podemos usar "contains"
            dimensions.add(dimension); // Cambié "put" por "add" (json-simple usa "add" en JSONArray)
        }

        json.put(teamName, dimensions);
        saveJsonFile(json);
    }
    public void delDimensionRegister(String teamName, String dimension) {
        JSONObject json = readJsonFile();
        JSONArray dimensions = (JSONArray) json.get(teamName);

        if (dimensions == null) return;

        if (dimensions.contains(dimension)) {
            dimensions.remove(dimension);
            json.put(teamName, dimensions);
            saveJsonFile(json);
        }
    }
    public boolean hasRegisteredDimension(String teamName, String dimension) {
        // Obtener los bosses asesinados por el equipo
        List<String> dimensionsRegistered = getDimensionsRegistered(teamName);

        // Verificar si el boss específico está en la lista de bosses asesinados
        return dimensionsRegistered.contains(dimension);
    }

    private List<String> getDimensionsRegistered(String teamName) {
        JSONObject json = readJsonFile();
        List<String> dimensionsRegistered = new ArrayList<>();

        if (json.containsKey(teamName)) { // Cambié "has" por "containsKey"
            JSONArray array = (JSONArray) json.get(teamName); // Cambié "optJSONArray" por "get"
            if (array != null) {
                for (Object obj : array) {
                    dimensionsRegistered.add(obj.toString()); // "optString" no existe en json-simple
                }
            }
        }
        return dimensionsRegistered;
    }

    public void addKilledBoss(String teamName, String bossName) {
        JSONObject json = readJsonFile();
        JSONArray killedBosses = (JSONArray) json.get(teamName); // Cambié "optJSONArray" por "get"

        if (killedBosses == null) {
            killedBosses = new JSONArray();
        }

        if (!killedBosses.contains(bossName)) { // "toList" no existe en json-simple, pero podemos usar "contains"
            killedBosses.add(bossName); // Cambié "put" por "add" (json-simple usa "add" en JSONArray)
        }

        json.put(teamName, killedBosses);
        saveJsonFile(json);
    }
    public void delKilledBoss(String teamName, String bossName) {
        JSONObject json = readJsonFile();
        JSONArray killedBosses = (JSONArray) json.get(teamName);

        if (killedBosses == null) return;

        if (killedBosses.contains(bossName)) {
            killedBosses.remove(bossName);
            json.put(teamName, killedBosses);
            saveJsonFile(json);
        }
    }
    public boolean hasKilledRequiredBosses(String teamName) {
        // Lista de bosses que deben ser derrotados en orden
        List<String> requiredBosses = Arrays.asList("wither", "elder_guardian", "warden"); // Puedes añadir otros bosses según sea necesario

        // Obtén los bosses que ya han sido asesinados por el equipo
        List<String> killedBosses = getKilledBosses(teamName);

        // Verifica si el equipo ha matado todos los bosses requeridos
        return new HashSet<>(killedBosses).containsAll(requiredBosses);
    }
    public boolean hasKilledBoss(String teamName, String bossName) {
        // Obtener los bosses asesinados por el equipo
        List<String> killedBosses = getKilledBosses(teamName);

        // Verificar si el boss específico está en la lista de bosses asesinados
        return killedBosses.contains(bossName);
    }

    public JSONObject readJsonFile() {
        if (!dataFile.exists() || dataFile.length() == 0) {
            System.out.println("[TeamDataManager] Archivo vacío o inexistente: " + dataFile.getAbsolutePath());
            return new JSONObject();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8))) {
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            System.out.println("[TeamDataManager] Error al leer el archivo JSON: " + e.getMessage());
            return new JSONObject();
        }
    }

    public void saveJsonFile(JSONObject json) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(dataFile), StandardCharsets.UTF_8)) {
            writer.write(json.toJSONString());
        } catch (IOException e) {
            System.out.println("[TeamDataManager] Error al guardar el archivo JSON: " + e.getMessage());
        }
    }
}
