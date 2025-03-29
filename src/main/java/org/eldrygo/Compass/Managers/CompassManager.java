package org.eldrygo.Compass.Managers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Material;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Utils.ChatUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CompassManager {
    private static ConfigManager configManager;
    private static ChatUtils chatUtils;

    public static void giveTrackingCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        if (meta != null) {
            meta.setLodestoneTracked(false);
            meta.setDisplayName(ChatUtils.formatColor(configManager.getMessageConfig().getString("compass.item.name", "%prefix% #FF0000&l[ERROR] #FF3535Message not found: compass.item.name")));
            List<String> lore = configManager.getMessageConfig().getStringList("compass.item.lore").stream().map(ChatUtils::formatColor).collect(Collectors.toList());
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

    public static void updateCompassTarget(Player player, ItemStack compass, Structure structureType) {
        World world = player.getWorld();
        Location structureLocation = (Location) world.locateNearestStructure(player.getLocation(), structureType, 10000, false);
        if (structureLocation != null) {
            CompassMeta meta = (CompassMeta) compass.getItemMeta();
            if (meta != null) {
                meta.setLodestone(structureLocation);
                meta.setLodestoneTracked(false);
                compass.setItemMeta(meta);
            }
            String structureName = structureType == Structure.MONUMENT ? configManager.getMessageString("compass.structure.monument") : configManager.getMessageString("compass.structure.ancient_city");
            player.sendMessage(chatUtils.getMessage("compass.use.update.success", player)
                    .replace("%structure%", structureName));
        } else {
            player.sendMessage(chatUtils.getMessage("compass.use.update.not_found", player));
        }
    }
}
