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
    private ConfigManager configManager;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.COMPASS && item.getItemMeta().getCustomModelData() == 1) {
            CompassMeta meta = (CompassMeta) item.getItemMeta();
            if (meta != null) {
                if (event.getAction().toString().contains("RIGHT_CLICK")) {
                    Structure target = meta.getLodestone() != null && meta.getDisplayName().contains(configManager.getMessageString("compass.structure.ancient_city"))
                            ? Structure.ANCIENT_CITY
                            : Structure.MONUMENT;
                    CompassManager.updateCompassTarget(player, item, target);
                } else if (event.getAction().toString().contains("LEFT_CLICK")) {
                    if (meta.getLodestone() != null && meta.getDisplayName().contains(configManager.getMessageString("compass.structure.ancient_city"))) {
                        meta.setDisplayName(ChatUtils.formatColor(configManager.getMessageString("compass.item.name")
                                        .replace("%structure%", configManager.getMessageString("compass.structure.monument"))));
                    } else {
                        meta.setDisplayName(ChatUtils.formatColor(configManager.getMessageString("compass.item.name")
                                        .replace("%structure%", configManager.getMessageString("compass.structure.ancient_city"))));
                    }
                    item.setItemMeta(meta);
                    player.sendMessage("Modo de rastreo cambiado.");
                }
            }
        }
    }
}
