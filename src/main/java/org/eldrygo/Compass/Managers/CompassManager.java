package org.eldrygo.Compass.Managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Material;
import org.eldrygo.Compass.Listeners.LocateCommandListener;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CompassManager {
    private final SpeedrunBoss plugin;
    private final ConfigManager configManager;
    private final ChatUtils chatUtils;
    private final LocateCommandListener locateCommandListener;

    public CompassManager(SpeedrunBoss plugin, ConfigManager configManager, ChatUtils chatUtils, LocateCommandListener locateCommandListener) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.chatUtils = chatUtils;
        this.locateCommandListener = locateCommandListener;
    }

    // Método estático
    public void updateCompassTarget(final Player player, final ItemStack compass, final Structure structureType) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }

        // Ejecutar el comando /locate de manera asíncrona para evitar bloqueos del hilo principal
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                // Determinar el tipo de estructura que se busca
                String structureName = structureType == Structure.MONUMENT
                        ? "ocean_monument"
                        : "ancient_city"; // Usa los nombres correctos de las estructuras

                // Ejecutar el comando locate y obtener la respuesta
                String command = "/locate " + structureName;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                // Esperamos un poco a que el comando sea ejecutado y obtener la ubicación
                // Asumimos que existe alguna forma de obtener la respuesta de /locate,
                // por ejemplo, un sistema de mensajes en el servidor.

                // Aquí puedes usar un sistema de escucha o algún tipo de procesamiento para capturar
                // el resultado de la búsqueda del comando y luego actualizar el compás.
                // Ejemplo básico para obtener la ubicación (esto depende de la forma en que se capture la salida):
                String result = locateCommandListener.getLocateResult(); // Método que captura el resultado del comando

                if (result != null && !result.isEmpty()) {
                    String[] resultParts = result.split(":");
                    if (resultParts.length == 2) {
                        try {
                            int x = Integer.parseInt(resultParts[0]);
                            int z = Integer.parseInt(resultParts[1]);

                            // Crear una ubicación en las coordenadas obtenidas
                            World world = player.getWorld();
                            Location structureLocation = new Location(world, x, world.getHighestBlockYAt(x, z), z);

                            CompassMeta meta = (CompassMeta) compass.getItemMeta();
                            if (meta != null) {
                                // Establecer la piedra de lodestone en la ubicación de la estructura
                                meta.setLodestone(structureLocation);
                                meta.setLodestoneTracked(true); // Asegúrate de que la aguja apunte hacia la estructura
                                compass.setItemMeta(meta); // Aplicar los cambios al compás

                                // Enviar mensaje al jugador indicando que el compás está apuntando a la estructura
                                player.sendMessage(chatUtils.getMessage("compass.use.update.success", player)
                                        .replace("%structure%", structureName));
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(chatUtils.getMessage("compass.use.update.not_found", player));
                        }
                    }
                } else {
                    player.sendMessage(chatUtils.getMessage("compass.use.update.not_found", player));
                }
            }
        });
    }

    // Método no estático
    public void giveTrackingCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        if (meta != null) {
            meta.setLodestoneTracked(false);
            meta.setDisplayName(chatUtils.formatColor(configManager.getMessageConfig().getString("compass.item.name", "%prefix% #FF0000&l[ERROR] #FF3535Message not found: compass.item.name")));
            List<String> lore = configManager.getMessageConfig().getStringList("compass.item.lore").stream()
                    .map(ChatUtils::formatColor).collect(Collectors.toList());
            meta.setLore(lore);

            if (configManager.getMessageConfig().getBoolean("compass.item.enchanted", false)) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            if (configManager.getMessageConfig().contains("compass.item.custom_model_data")) {
                meta.setCustomModelData(configManager.getMessageConfig().getInt("compass.item.custom_model_data"));
            }

            compass.setItemMeta(meta);
        }
        player.getInventory().addItem(compass);
    }
}
