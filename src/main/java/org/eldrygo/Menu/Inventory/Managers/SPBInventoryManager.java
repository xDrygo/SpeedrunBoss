package org.eldrygo.Menu.Inventory.Managers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Menu.Inventory.Model.InventoryPlayer;
import org.eldrygo.Menu.Inventory.Model.InventorySection;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.Utils.InventoryUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;

import java.util.*;

public class SPBInventoryManager {
    private final ConfigManager configManager;
    private final InventoryUtils inventoryUtils;
    private final XTeamsAPI xTeamsAPI;

    private final ArrayList<InventoryPlayer> players;
    private final SpeedrunBoss plugin;

    public SPBInventoryManager(ConfigManager configManager, InventoryUtils inventoryUtils, XTeamsAPI xTeamsAPI, SpeedrunBoss plugin) {
        this.configManager = configManager;
        this.inventoryUtils = inventoryUtils;
        this.xTeamsAPI = xTeamsAPI;
        this.plugin = plugin;
        this.players = new ArrayList<>();
    }

    public InventoryPlayer getInventoryPlayer(Player player) {
        for(InventoryPlayer inventoryPlayer : players) {
            if (inventoryPlayer.getPlayer().equals(player)) {
                return inventoryPlayer;
            }
        }
        return null;
    }

    public void removePlayer(Player player) {
        players.removeIf(inventoryPlayer -> inventoryPlayer.getPlayer().equals(player));
    }

    public void openMainInventory(InventoryPlayer inventoryPlayer) {
        inventoryPlayer.setSection(InventorySection.MENU_MAIN);
        Player player = inventoryPlayer.getPlayer();
        Inventory inv = Bukkit.createInventory(null, 27, getMessage("main.title"));


        // BLACK STAINED GLASS ON CORNERS
        inv.setItem(0, inventoryUtils.getMainCornerItem());
        inv.setItem(8, inventoryUtils.getMainCornerItem());
        inv.setItem(18, inventoryUtils.getMainCornerItem());
        inv.setItem(26, inventoryUtils.getMainCornerItem());
        inv.setItem(1, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(7, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(9, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(17, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(19, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(25, inventoryUtils.getTeamsSecondCornerItem());

        // FUNCTION ITEMS
        inv.setItem(11, inventoryUtils.getMainTeamsItem());
        inv.setItem(22, inventoryUtils.getCloseItem());

        player.openInventory(inv);
        players.add(inventoryPlayer);
    }

    private static final int[] TEAM_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
        18, 19, 20, 21, 22, 23, 24, 25, 26
    };

    private static final List<String> DISPLAY_TEAMS = Arrays.asList(
            "team_1", "team_2", "team_3", "team_4",
            "team_5", "team_6", "team_7", "team_8",
            "team_9", "team_10", "team_11", "team_12",
            "team_13", "team_14", "team_15", "team_16"
    );

    private static final int[] TEAM_PLAYER_SLOTS = {
                1
    };

    public Inventory createTeamsInventory() {
        Inventory inv = Bukkit.createInventory(null, 45, getMessage("teams.title"));

        int slotIndex = 0;

        inv.setItem(0, inventoryUtils.getTeamsCornerItem());
        inv.setItem(8, inventoryUtils.getTeamsCornerItem());
        inv.setItem(36, inventoryUtils.getTeamsCornerItem());
        inv.setItem(44, inventoryUtils.getTeamsCornerItem());
        inv.setItem(1, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(7, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(37, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(43, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(41, inventoryUtils.getCloseItem());
        inv.setItem(39, inventoryUtils.getBackItem());

        for (String teamName : DISPLAY_TEAMS) {
            String teamDisplayName = xTeamsAPI.getTeamDisplayName(teamName);
            Set<String> teamMembers = Collections.singleton("#FF3535Equipo vacío.");
            if (!xTeamsAPI.getTeamMembers(teamName).isEmpty()) {
                teamMembers = xTeamsAPI.getTeamMembers(teamName);
            }

            ItemStack teamItem = inventoryUtils.getTeamItem(teamName, teamDisplayName, teamMembers);

            if (slotIndex < TEAM_SLOTS.length) {
                inv.setItem(TEAM_SLOTS[slotIndex], teamItem);
            }
            slotIndex++;
        }

        return inv;
    }

    public void openTeamsInventory(InventoryPlayer inventoryPlayer) {
        inventoryPlayer.setSection(InventorySection.MENU_TEAMS);
        Player player = inventoryPlayer.getPlayer();
        Inventory inventory = createTeamsInventory();
        player.openInventory(inventory);
        players.add(inventoryPlayer);
    }

    public void openPerTeamInventory(String teamName, InventoryPlayer inventoryPlayer) {
        inventoryPlayer.setSection(InventorySection.MENU_PER_TEAM);
        Player player = inventoryPlayer.getPlayer();
        Set<String> teamMembers = xTeamsAPI.getTeamMembers(teamName);

        Inventory inv = Bukkit.createInventory(null, 27, getMessage("per_team_inv.title")
                .replace("%team_displayname%", xTeamsAPI.getTeamDisplayName(teamName)));

        int[] memberSlots = {11, 15};
        inv.setItem(0, inventoryUtils.getTeamsCornerItem());
        inv.setItem(8, inventoryUtils.getTeamsCornerItem());
        inv.setItem(18, inventoryUtils.getTeamsCornerItem());
        inv.setItem(26, inventoryUtils.getTeamsCornerItem());
        inv.setItem(1, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(7, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(9, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(17, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(19, inventoryUtils.getTeamsSecondCornerItem());
        inv.setItem(25, inventoryUtils.getTeamsSecondCornerItem());

        // Agrega los ítems "Vacío" por defecto
        for (int slot : memberSlots) {
            inv.setItem(slot, inventoryUtils.getEmptyMemberItem());
        }

        // Luego reemplaza con las cabezas reales
        int index = 0;
        for (String memberName : teamMembers) {
            if (index >= memberSlots.length) break;

            ItemStack skull = inventoryUtils.getPlayerHead(memberName);
            inv.setItem(memberSlots[index], skull);
            index++;
        }

        inv.setItem(23, inventoryUtils.getCloseItem());
        inv.setItem(21, inventoryUtils.getBackItem());
        player.openInventory(inv);
        players.add(inventoryPlayer);
    }



    public void inventoryClick(InventoryPlayer inventoryPlayer, int slot, InventoryClickEvent event) {
        Player player = inventoryPlayer.getPlayer();
        InventorySection section = inventoryPlayer.getSection();

        if (section.equals(InventorySection.MENU_MAIN)) {
            if (slot == 11) {
                openTeamsInventory(inventoryPlayer);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 2f);
            }
            if (slot == 22) {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 0.1f);
            }
        } else if(section.equals(InventorySection.MENU_TEAMS)) {
            if (Arrays.stream(TEAM_SLOTS).anyMatch(i -> i == slot)) {
                openPerTeamInventory(getTeamForSlot(slot), inventoryPlayer);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 2f);
            }
            if (slot == 41) {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 0.1f);
            }
            if (slot == 39) {
                openMainInventory(inventoryPlayer);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
            }
        } else if (section.equals(InventorySection.MENU_PER_TEAM)) {
            if (slot == 23) {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 0.1f);
                return;
            }
            if (slot == 21) {
                openTeamsInventory(inventoryPlayer);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f);
            }
            if (slot == 11 || slot == 15) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null) return;

                // Solo continuar si es una cabeza de jugador
                if (clickedItem.getType() != Material.PLAYER_HEAD) return;

                ItemMeta meta = clickedItem.getItemMeta();
                if (meta == null || !meta.hasDisplayName()) return;

                // Obtener el nombre del jugador desde la tag personalizada
                NamespacedKey key = new NamespacedKey(plugin, "player_name");
                String playerName = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

                if (playerName != null) {
                    Player target = Bukkit.getPlayerExact(playerName);
                    if (target != null && target.isOnline()) {
                        player.teleport(target.getLocation());
                        player.sendMessage(getMessage("per_team_inv.methods.tp.success").replace("%player%", target.getName()));
                        player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1f, 0.1f);
                        player.setGameMode(GameMode.SPECTATOR);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 0));
                        player.closeInventory();
                    } else {
                        player.sendMessage(getMessage("per_team_inv.methods.tp.not_online"));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    }
                }
            }
        }
    }

    private String getMessage(String path) {
        return ChatUtils.formatColor(configManager.getInventoriesConfig().getString(path)).replace("%prefix%", plugin.prefix);
    }
    public String getTeamForSlot(int slot) {
        switch (slot) {
            case 10:
                return "team_1";
            case 11:
                return "team_2";
            case 12:
                return "team_3";
            case 13:
                return "team_4";
            case 14:
                return "team_5";
            case 15:
                return "team_6";
            case 16:
                return "team_7";
            case 18:
                return "team_8";
            case 19:
                return "team_9";
            case 20:
                return "team_10";
            case 21:
                return "team_11";
            case 22:
                return "team_12";
            case 23:
                return "team_13";
            case 24:
                return "team_14";
            case 25:
                return "team_15";
            case 26:
                return "team_16";
            default:
                return "null";
        }
    }
}
