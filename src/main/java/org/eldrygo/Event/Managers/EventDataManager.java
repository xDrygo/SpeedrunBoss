package org.eldrygo.Event.Managers;

import org.eldrygo.Event.Managers.EventManager.EventState;
import org.eldrygo.SpeedrunBoss;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventDataManager {
    private final File dataFile;

    public EventDataManager(SpeedrunBoss plugin) {
        this.dataFile = new File(plugin.getDataFolder(), "event_data.json");
        if (!dataFile.exists()) {
            saveEventState(EventState.NOT_STARTED, new HashSet<>());
        }
    }

    public void saveEventState(EventState state, Set<UUID> participants) {
        JSONObject json = new JSONObject();
        json.put("state", state.toString());

        JSONArray participantArray = new JSONArray();
        for (UUID uuid : participants) {
            participantArray.add(uuid.toString());
        }
        json.put("participants", participantArray);

        try (FileWriter writer = new FileWriter(dataFile, StandardCharsets.UTF_8)) {
            writer.write(json.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadEventState(EventManager eventManager) {
        if (!dataFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile, StandardCharsets.UTF_8))) {
            JSONObject json = (JSONObject) new org.json.simple.parser.JSONParser().parse(reader);

            EventState state = EventState.valueOf((String) json.get("state"));
            JSONArray participantArray = (JSONArray) json.get("participants");

            Set<UUID> participants = new HashSet<>();
            for (Object obj : participantArray) {
                participants.add(UUID.fromString((String) obj));
            }

            // Aplicar el estado al evento
            eventManager.resumeEvent(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearEventData() {
        if (dataFile.exists()) {
            dataFile.delete();
        }
    }
}
