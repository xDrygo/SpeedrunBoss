package org.eldrygo.Compass.Managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.util.StructureSearchResult;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.Utils.SettingsUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CompassManager {
    private final SpeedrunBoss plugin;
    private final ConfigManager configManager;
    private final ChatUtils chatUtils;

    public CompassManager(SpeedrunBoss plugin, ConfigManager configManager, ChatUtils chatUtils) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.chatUtils = chatUtils;
    }

    public void updateCompassTarget(Player player, ItemStack compass, Structure structureType) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Location origin = player.getLocation();
            World world = player.getWorld();
            int searchRadius = plugin.getConfig().getInt("settings.compass_search_radius");

            StructureSearchResult result;
            try {
                result = world.locateNearestStructure(origin, structureType, searchRadius, false);
            } catch (Exception e) {
                player.sendMessage(chatUtils.getMessage("compass.use.update.error", player));
                return;
            }

            if (result == null || result.getLocation() == null) {
                player.sendMessage(chatUtils.getMessage("compass.use.update.not_found", player));
                return;
            }

            Location structureLocation = result.getLocation();
            CompassMeta meta = (CompassMeta) compass.getItemMeta();
            if (meta != null) {
                meta.setLodestone(structureLocation);
                meta.setLodestoneTracked(true);
                compass.setItemMeta(meta);

                String structureName = structureType.getKey().getKey();
                if (structureType.getKey().getNamespace().equals("minecraft") && structureName.equals("jigsaw")) {
                    structureName = "ancient_city";
                }

                String formattedStructureName;
                switch (structureName) {
                    case "ancient_city":
                        formattedStructureName = ChatUtils.formatColor(configManager.getMessageConfig().getString("compass.structure.ancient_city", "Ancient City"));
                        break;
                    case "monument":
                        formattedStructureName = ChatUtils.formatColor(configManager.getMessageConfig().getString("compass.structure.monument", "Monument"));
                        break;
                    default:
                        formattedStructureName = ChatUtils.formatColor(configManager.getMessageConfig().getString("compass.structure.null", "Unknown"));
                }

                player.sendMessage(chatUtils.getMessage("compass.use.update.success", player)
                        .replace("%structure%", formattedStructureName));
            }
        });
    }


    public void giveTrackingCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();

        if (meta != null) {
            meta.setLodestoneTracked(false);

            // Nombre
            String displayName = configManager.getMessageConfig().getString("compass.item.name.no_structure",
                    "&r%prefix% #FF0000&l[ERROR] #FF3535Message not found: compass.item.name.no_structure");
            meta.setDisplayName(chatUtils.formatColor(displayName));

            // Lore
            List<String> lore = configManager.getMessageConfig().getStringList("compass.item.lore").stream()
                    .map(ChatUtils::formatColor)
                    .collect(Collectors.toList());
            meta.setLore(lore);

            // Encantamiento
            if (configManager.getMessageConfig().getBoolean("compass.item.enchanted", false)) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            // Modelo
            if (configManager.getMessageConfig().contains("compass.item.custom_model_data")) {
                meta.setCustomModelData(configManager.getMessageConfig().getInt("compass.item.custom_model_data"));
            }

            compass.setItemMeta(meta);
        }

        player.getInventory().addItem(compass);
    }
}
