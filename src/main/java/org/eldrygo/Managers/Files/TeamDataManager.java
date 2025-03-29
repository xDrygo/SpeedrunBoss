package org.eldrygo.Managers.Files;

import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamDataManager {
    private final File dataFile;

    public TeamDataManager(JavaPlugin plugin) {
        this.dataFile = new File(plugin.getDataFolder(), "teams.json");

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
    public boolean hasKilledRequiredBosses(String teamName) {
        // Lista de bosses que deben ser derrotados en orden
        List<String> requiredBosses = Arrays.asList("wither", "elder_guardian", "warden"); // Puedes añadir otros bosses según sea necesario

        // Obtén los bosses que ya han sido asesinados por el equipo
        List<String> killedBosses = getKilledBosses(teamName);

        // Verifica si el equipo ha matado todos los bosses requeridos
        return killedBosses.containsAll(requiredBosses);
    }
    public boolean hasKilledBoss(String teamName, String bossName) {
        // Obtener los bosses asesinados por el equipo
        List<String> killedBosses = getKilledBosses(teamName);

        // Verificar si el boss específico está en la lista de bosses asesinados
        return killedBosses.contains(bossName);
    }

    public JSONObject readJsonFile() {
        if (!dataFile.exists()) return new JSONObject();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8))) {
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(reader); // Se usa JSONParser en json-simple
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public void saveJsonFile(JSONObject json) {
        try (FileWriter writer = new FileWriter(dataFile, StandardCharsets.UTF_8)) {
            writer.write(json.toJSONString()); // "toString(int)" no existe en json-simple, se usa "toJSONString()"
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
