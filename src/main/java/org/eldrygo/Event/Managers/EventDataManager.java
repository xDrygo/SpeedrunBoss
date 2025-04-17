package org.eldrygo.Event.Managers;

import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.SpeedrunBoss;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventDataManager {
    private final SpeedrunBoss plugin;
    private final File dataFile;

    public EventDataManager(SpeedrunBoss plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data/event.json");
        if (!dataFile.getParentFile().exists()) {
            dataFile.getParentFile().mkdirs();
        }
        if (!dataFile.exists()) {
            saveDefaultEventData();
        }
    }

    private void saveDefaultEventData() {
        JSONObject json = new JSONObject();
        json.put("state", EventManager.EventState.NOT_STARTED.toString());
        json.put("participants", new JSONArray());
        saveJson(json);
    }

    public void saveEventState(EventManager.EventState state, Set<UUID> participants) {
        if (state == null) state = EventManager.EventState.NOT_STARTED;
        if (participants == null) participants = new HashSet<>();

        JSONObject json = readJson();
        json.put("state", state.toString());

        JSONArray participantArray = new JSONArray();
        for (UUID uuid : participants) {
            participantArray.add(uuid.toString());
        }
        json.put("participants", participantArray);

        saveJson(json);
    }

    private JSONObject readJson() {
        if (!dataFile.exists()) return new JSONObject();
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile, StandardCharsets.UTF_8))) {
            return (JSONObject) new org.json.simple.parser.JSONParser().parse(reader);
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Failed reading event.json: " + e.getMessage());
            e.printStackTrace();
            return new JSONObject();
        }
    }

    private void saveJson(JSONObject json) {
        try (FileWriter writer = new FileWriter(dataFile, StandardCharsets.UTF_8)) {
            writer.write(json.toJSONString());
        } catch (IOException e) {
            plugin.getLogger().severe("❌ Failed saving event.json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void clearEventData() {
        if (dataFile.exists()) {
            dataFile.delete();
        }
    }

    public void loadEventState(EventManager eventManager) {
        if (!dataFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile, StandardCharsets.UTF_8))) {
            JSONObject json = (JSONObject) new org.json.simple.parser.JSONParser().parse(reader);

            JSONArray participantArray = (JSONArray) json.get("participants");
            Set<UUID> participants = new HashSet<>();
            for (Object obj : participantArray) {
                participants.add(UUID.fromString((String) obj));
            }

            // Puedes usar el estado si es necesario más adelante
            eventManager.resumeEvent(null);
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Failed on loading Event State: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
