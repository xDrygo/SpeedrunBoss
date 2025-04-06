package org.eldrygo.Compass.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.eldrygo.Compass.Managers.CompassManager;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Utils.ChatUtils;

public class CompassListener implements Listener {
    private final ConfigManager configManager;
    private final CompassManager compassManager;

    public CompassListener(ConfigManager configManager, CompassManager compassManager) {
        this.configManager = configManager;
        this.compassManager = compassManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.COMPASS && item.getItemMeta().getCustomModelData() == 1) {
            CompassMeta meta = (CompassMeta) item.getItemMeta();
            if (meta != null) {
                // Si se hace clic derecho, actualizamos el objetivo del compás.
                if (event.getAction().toString().contains("RIGHT_CLICK")) {
                    Structure target = meta.getLodestone() != null && meta.getDisplayName().contains(configManager.getMessageString("compass.structure.ancient_city"))
                            ? Structure.ANCIENT_CITY
                            : Structure.MONUMENT;
                    compassManager.updateCompassTarget(player, item, target); // Actualiza el compás
                }
                // Si se hace clic izquierdo, cambiamos entre las dos estructuras (Monumento y Ciudad Ancestral).
                else if (event.getAction().toString().contains("LEFT_CLICK")) {
                    if (meta.getLodestone() != null && meta.getDisplayName().contains(configManager.getMessageString("compass.structure.ancient_city"))) {
                        // Cambiar a Monumento Oceánico
                        meta.setDisplayName(ChatUtils.formatColor(configManager.getMessageString("compass.item.name")
                                .replace("%structure%", configManager.getMessageString("compass.structure.monument"))));
                        // Actualizar el compás para rastrear el Monumento
                        compassManager.updateCompassTarget(player, item, Structure.MONUMENT);
                    } else {
                        // Cambiar a Ciudad Ancestral
                        meta.setDisplayName(ChatUtils.formatColor(configManager.getMessageString("compass.item.name")
                                .replace("%structure%", configManager.getMessageString("compass.structure.ancient_city"))));
                        // Actualizar el compás para rastrear la Ciudad Ancestral
                        compassManager.updateCompassTarget(player, item, Structure.ANCIENT_CITY);
                    }
                    item.setItemMeta(meta); // Aplicar los cambios al compás
                    player.sendMessage("Modo de rastreo cambiado.");
                }
            }
        }
    }
}
