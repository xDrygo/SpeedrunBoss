package org.eldrygo.Compass.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;
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

        if (item != null && item.getType() == Material.COMPASS && (item.getItemMeta().getCustomModelData() == 1 || item.getItemMeta().getCustomModelData() == 2 || item.getItemMeta().getCustomModelData() == 3)) {
            CompassMeta meta = (CompassMeta) item.getItemMeta();
            if (meta != null) {
                if (event.getAction().toString().contains("RIGHT_CLICK")) {
                    Structure target = meta.getLodestone() != null && meta.getCustomModelData() == 2  ? Structure.ANCIENT_CITY : Structure.MONUMENT;
                compassManager.updateCompassTarget(player, item, target);
                    player.sendMessage("Brujula actualizada.");
            }
            else if (event.getAction().toString().contains("LEFT_CLICK")) {
                if (meta.getLodestone() != null && meta.getCustomModelData() == 2) {
                    meta.setDisplayName(ChatUtils.formatColor(configManager.getMessageString("compass.item.name.structure")
                            .replace("%structure%", configManager.getMessageString("compass.structure.monument"))));
                    meta.setCustomModelData(3);
                    compassManager.updateCompassTarget(player, item, Structure.MONUMENT);
                    } else {
                    // Cambiar a Ciudad Ancestral
                    meta.setDisplayName(ChatUtils.formatColor(configManager.getMessageString("compass.item.name.structure")
                            .replace("%structure%", configManager.getMessageString("compass.structure.ancient_city"))));
                    meta.setCustomModelData(2);
                    compassManager.updateCompassTarget(player, item, Structure.ANCIENT_CITY);
                }
                    item.setItemMeta(meta);
                    player.sendMessage("Modo de rastreo cambiado.");
                }
            }
        }
    }
}
