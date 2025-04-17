package org.eldrygo.Time.Managers;

import org.eldrygo.SpeedrunBoss;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class TimeManager {

    private final File file;
    private long startTime;
    private long pauseTime;
    private boolean running;
    private boolean paused;

    public TimeManager(SpeedrunBoss plugin) {
        this.file = new File(plugin.getDataFolder(), "data/times.json");
        this.startTime = 0;
        this.pauseTime = 0;
        this.running = false;
        this.paused = false;

        // Asegúrate de que el archivo times.json exista
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            saveData(new JSONObject());
        }
    }

    // Inicia el cronómetro
    public void start() {
        if (running) {
            return; // Si ya está corriendo, no hacemos nada
        }
        this.startTime = System.currentTimeMillis();
        this.running = true;
        this.paused = false;
    }

    // Pausa el cronómetro
    public void pause() {
        if (running && !paused) {
            this.pauseTime = System.currentTimeMillis();
            this.paused = true;
        }
    }

    // Reanuda el cronómetro
    public void resume() {
        if (paused) {
            long pauseDuration = System.currentTimeMillis() - this.pauseTime;
            this.startTime += pauseDuration; // Ajusta el tiempo de inicio al tiempo pausado
            this.paused = false;
        }
    }

    // Detiene el cronómetro
    public void stop() {
        this.running = false;
        this.paused = false;
    }

    // Verifica si el cronómetro está corriendo
    public boolean isRunning() {
        return running && !paused;
    }

    // Devuelve el tiempo transcurrido en segundos
    public long getElapsedTime() {
        if (!running) return 0;
        long currentTime = paused ? pauseTime : System.currentTimeMillis();
        return (currentTime - startTime) / 1000;
    }

    // Cargar datos desde el archivo JSON
    private JSONObject loadData() {
        try (FileReader reader = new FileReader(file)) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    // Guardar datos en el archivo JSON
    private void saveData(JSONObject data) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(data.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Registra un boss asesinado con el tiempo del cronómetro
    public void recordBossKill(String teamName, String bossName) {
        if (!running) return;

        JSONObject data = loadData();
        JSONObject teamData = (JSONObject) data.getOrDefault(teamName, new JSONObject());

        // Registro del tiempo formateado
        long elapsedTime = getElapsedTime();
        String formatted = formatTime(elapsedTime);

        teamData.put(bossName, formatted);

        data.put(teamName, teamData);
        saveData(data);
    }

    // Obtiene el tiempo registrado para un boss de un equipo
    public String getTimeForBoss(String teamName, String bossName) {
        JSONObject data = loadData();
        JSONObject teamData = (JSONObject) data.get(teamName);

        if (teamData == null) {
            return "N/A"; // No se encontró el equipo
        }

        String time = (String) teamData.get(bossName);
        if (time == null) {
            return "N/A"; // No se encontró el boss
        }

        return time; // Ej: "01:22:34"
    }
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
