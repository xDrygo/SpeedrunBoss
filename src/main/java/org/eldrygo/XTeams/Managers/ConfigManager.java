package org.eldrygo.XTeams.Managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.XTeams.Models.Team;

import java.util.*;

public class ConfigManager {
    private final SpeedrunBoss plugin;

    public ConfigManager(SpeedrunBoss plugin) {
        this.plugin = plugin;
    }

    public void loadTeamsFromConfig() {
        FileConfiguration config = plugin.getConfig();

        if (config.contains("teams")) {
            for (String teamName : Objects.requireNonNull(config.getConfigurationSection("teams")).getKeys(false)) {
                List<String> players = config.getStringList("teams." + teamName + ".members");
                String displayName = config.getString("teams." + teamName + ".displayName", teamName);
                int priority = config.getInt("teams." + teamName + ".priority", 0);  // Si no hay displayName, usar el teamName

                // Crear el equipo con los jugadores y el displayName directamente desde la configuración
                Set<String> playerSet = new HashSet<>(players); // Convertir la lista de jugadores en un Set
                Team team = new Team(teamName, displayName, priority, playerSet);  // Usar el constructor adecuado
                plugin.getTeamManager().addTeam(team);  // CORRECCIÓN: Añadir el equipo con el nombre

                plugin.getLogger().info("Team loaded: " + teamName);
            }
        }
    }

    public void saveTeamsToConfig() {
        FileConfiguration config = plugin.getConfig();
        // Limpiar la sección de equipos para evitar duplicados
        config.set("teams", null);
        // Guardar cada equipo directamente en la configuración
        for (Team team : plugin.getTeamManager().getAllTeams()) { // Iteramos sobre los objetos Team
            if (team == null) {
                plugin.getLogger().warning("A null team was encountered, skipping save.");
                continue; // Si el equipo es null, lo saltamos
            }
            String teamName = team.getName(); // Obtener el nombre del equipo
            Set<String> members = team.getMembers(); // Obtener los miembros del equipo

            // Convertir el Set en una lista para que sea compatible con YAML
            List<String> memberList = new ArrayList<>(members);

            // Guardar los jugadores de ese equipo en la configuración
            config.set("teams." + teamName + ".members", memberList); // Usamos el teamName que es un String
            config.set("teams." + teamName + ".displayName", team.getDisplayName()); // Usamos el displayName que es un String
            config.set("teams." + teamName + ".priority", team.getPriority()); // Usamos el priority que es un integer
        }
        // Guardar la configuración en el archivo
        plugin.saveConfig();
    }
    public String getPrefix() { return plugin.xTeamsPrefix; }
}
