package org.eldrygo.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Managers.Files.PlayerDataManager;

import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Time.Managers.TimeManager;
import org.eldrygo.XTeams.API.XTeamsAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InventoryUtils {
    private final ConfigManager configManager;
    private final PlayerDataManager playerDataManager;
    private final SpeedrunBoss plugin;
    private final TimeManager timeManager;
    private final TeamDataManager teamDataManager;
    private final XTeamsAPI xTeamsAPI;

    public InventoryUtils(ConfigManager configManager, PlayerDataManager playerDataManager, SpeedrunBoss plugin, TimeManager timeManager, TeamDataManager teamDataManager, XTeamsAPI xTeamsAPI) {
        this.configManager = configManager;
        this.playerDataManager = playerDataManager;
        this.plugin = plugin;
        this.timeManager = timeManager;
        this.teamDataManager = teamDataManager;
        this.xTeamsAPI = xTeamsAPI;
    }

    public ItemStack getCloseItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getMessage("genericitems.close.name"));
        item.setItemMeta(meta);


        return item;
    }
    // MAIN INVENTORY
        // DECORATIVE ITEMS:
    public ItemStack getMainCornerItem() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);

        return item;
    }
    public ItemStack getTeamsSecondCornerItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);

        return item;
    }
        // FUNCTION ITEMS:

    public ItemStack getMainTeamsItem() {
        ItemStack item = new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(getMessage("main.items.teams.name"));
            List<String> lore = getStringMessage("main.items.teams.lore").stream()
                    .map(ChatUtils::formatColor)
                    .collect(Collectors.toList());
            meta.setLore(lore);
            meta.addItemFlags(
                    ItemFlag.HIDE_DYE,
                    ItemFlag.HIDE_ATTRIBUTES
            );
            item.setItemMeta(meta);
        }

        return item;
    }


    public ItemStack getTeamItem(String teamName, String teamDisplayName, Set<String> teamMembers) {
        // Obtener la configuración de los items
        List<String> rawLore = configManager.getInventoriesConfig().getStringList("teams.inventory.lore");

        // Reemplazar las variables del lore
        List<String> lore = new ArrayList<>();
        for (String line : rawLore) {
            String formattedLine = line
                    .replace("%team%", teamName)
                    .replace("%team_displayname%", teamDisplayName)
                    .replace("%team_members%", String.join(", ", teamMembers));
            lore.add(ChatUtils.formatColor(formattedLine));
        }

        // Crear el ItemStack de cuero
        ItemStack item = new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(getMessage("teams.inventory.name." + teamName));
            meta.setLore(lore);

            meta.addItemFlags(
                    ItemFlag.HIDE_DYE,
                    ItemFlag.HIDE_ATTRIBUTES
            );

            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack getTeamInfoItem(String teamName) {
        String wardenTime = getMessage("per_team_inv.items.info.boss_not_registered");
        String elderGuardianTime = getMessage("per_team_inv.items.info.boss_not_registered");
        String witherTime = getMessage("per_team_inv.items.info.boss_not_registered");
        String enderDragonTime = getMessage("per_team_inv.items.info.boss_not_registered");
        if (teamDataManager.getKilledBosses(teamName).contains("warden")) { wardenTime = timeManager.getTimeForBoss(teamName, "warden"); }
        if (teamDataManager.getKilledBosses(teamName).contains("elder_guardian")) { elderGuardianTime = timeManager.getTimeForBoss(teamName, "elder_guardian"); }
        if (teamDataManager.getKilledBosses(teamName).contains("wither")) { witherTime = timeManager.getTimeForBoss(teamName, "wither"); }
        if (teamDataManager.getKilledBosses(teamName).contains("ender_dragon")) { enderDragonTime = timeManager.getTimeForBoss(teamName, "ender_dragon"); }

        List<String> rawLore = configManager.getInventoriesConfig().getStringList("per_team_inv.items.info.lore");
        List<String> lore = new ArrayList<>();
        for (String line : rawLore) {
            String formattedLine = line
                    .replace("%warden_time%", wardenTime)
                    .replace("%elder_guardian_time%", elderGuardianTime)
                    .replace("%wither_time%", witherTime)
                    .replace("%ender_dragon_time%", enderDragonTime);
            lore.add(ChatUtils.formatColor(formattedLine));
        }
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(getMessage("per_team_inv.items.info.name").replace("%team_display%", xTeamsAPI.getTeamDisplayName(teamName)));
            meta.setLore(lore);

            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack getPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        List<String> rawLore = configManager.getInventoriesConfig().getStringList("per_team_inv.items.player.lore");

        // Reemplazar las variables del lore
        List<String> lore = new ArrayList<>();
        Player onlinePlayer = Bukkit.getPlayer(playerName); // Verificación si el jugador está en línea
        String skullOwnerName = onlinePlayer != null ? playerName : "pantheress";
        for (String line : rawLore) {
            String formattedLine = line
                    .replace("%x%", onlinePlayer != null ? String.valueOf((int) onlinePlayer.getX()) : "#FF3535✘")
                    .replace("%y%", onlinePlayer != null ? String.valueOf((int) onlinePlayer.getY()) : "#FF3535✘")
                    .replace("%z%", onlinePlayer != null ? String.valueOf((int) onlinePlayer.getZ()) : "#FF3535✘")
                    .replace("%status%", playerDataManager.getStatus(playerName).toUpperCase());
            lore.add(ChatUtils.formatColor(formattedLine));
        }

        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(skullOwnerName));
            meta.setDisplayName(ChatUtils.formatColor(configManager.getInventoriesConfig().getString("per_team_inv.items.player.name"))
                    .replace("%player%", playerName));

            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            NamespacedKey key = new NamespacedKey(plugin, "player_name");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, playerName);

            head.setItemMeta(meta);
        }

        return head;
    }
    // UTIL METHODS

    private String getMessage(String path) {
        return ChatUtils.formatColor(configManager.getInventoriesConfig().getString(path));
    }
    private List<String> getStringMessage(String path) {
        return configManager.getInventoriesConfig().getStringList(path);
    }

    public ItemStack getTeamsCornerItem() {
        ItemStack item = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);

        return item;
    }

    public ItemStack getEmptyMemberItem() {
        ItemStack item = new ItemStack(Material.STRUCTURE_VOID);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Obtener el nombre y lore desde la configuración
            String displayName = configManager.getInventoriesConfig().getString("per_team_inv.items.empty.name");
            List<String> rawLore = configManager.getInventoriesConfig().getStringList("per_team_inv.items.empty.lore");

            if (displayName == null || displayName.isEmpty()) {
                displayName = "&f&lᴊᴜɢᴀᴅᴏʀ&r &7› #ff3535Vacío";
            }

            List<String> lore = new ArrayList<>();
            for (String line : rawLore) {
                lore.add(ChatUtils.formatColor(line));
            }

            meta.setDisplayName(ChatUtils.formatColor(displayName));
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        item.setItemMeta(meta);

        return item;
    }

    public ItemStack getBackItem() {
        ItemStack item = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getMessage("genericitems.back.name"));
        item.setItemMeta(meta);


        return item;
    }
}
