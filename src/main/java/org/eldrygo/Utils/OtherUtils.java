package org.eldrygo.Utils;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.XTeams.API.XTeamsAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OtherUtils {
    private final SpeedrunBoss plugin;
    private final XTeamsAPI xTeamsAPI;
    private final ConfigManager configManager;
    private final ChatUtils chatUtils;

    public OtherUtils(SpeedrunBoss plugin, XTeamsAPI xTeamsAPI, ConfigManager configManager, ChatUtils chatUtils) {
        this.plugin = plugin;
        this.xTeamsAPI = xTeamsAPI;
        this.configManager = configManager;
        this.chatUtils = chatUtils;
    }

    public static void applyKeepInventoryToAllWorlds() {
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
        }
    }

    public String getWinnerTeamDisplay() {
        String teamName = plugin.winnerTeam;
        if (teamName == null) teamName = "staff";

        try {
            String display = plugin.getTeamManager().getTeamDisplayName(teamName);
            return display != null ? display : "ः";
        } catch (Exception e) {
            return "ः";
        }
    }

    public String getWinnerTeamMembers() {
        String teamName = plugin.winnerTeam;
        List<String> teamMembers = new ArrayList<>(xTeamsAPI.getTeamMembers(teamName));

        String noMembersMsg = " ";
        String memberFormat = configManager.getMessageString("cinematics.winner.titles.winner.teammates");
        String separator = configManager.getMessageString("cinematics.winner.titles.winner.teammates_separator");

        String teamMembersFormatted = teamMembers.isEmpty() ? noMembersMsg :
                teamMembers.stream()
                        .map(member -> memberFormat.replace("%member_name%", member))
                        .collect(Collectors.joining(separator));

        return ChatUtils.formatColor(teamMembersFormatted);
    }

    public String getTeamMembers(String teamName, Player player) {
        List<String> teamMembers = new ArrayList<>(xTeamsAPI.getTeamMembers(teamName));

        teamMembers.removeIf(member -> member.equalsIgnoreCase(player.getName()));

        String noMembersMsg = " ";
        String memberFormat = "%member_name%";
        String separator = ", ";

        String teamMembersFormatted = teamMembers.isEmpty() ? noMembersMsg :
                teamMembers.stream()
                        .map(member -> memberFormat.replace("%member_name%", member))
                        .collect(Collectors.joining(separator));

        return ChatUtils.formatColor(teamMembersFormatted);
    }

    public ItemStack getWinnerTrophy(Player player, String teamName) {
        List<String> rawLore = configManager.getMessageConfig().getStringList("trophy.winner.lore");

        String teamDisplayName = Optional.ofNullable(xTeamsAPI.getTeamDisplayName(teamName)).orElse("Unknown Team");
        String teamMembers = Optional.ofNullable(getTeamMembers(teamName, player)).orElse("No teammates");

        List<String> lore = new ArrayList<>();
        for (String line : rawLore) {
            String formattedLine = line
                    .replace("%player%", Optional.ofNullable(player.getName()).orElse("Unknown"))
                    .replace("%team%", teamDisplayName)
                    .replace("%teammate%", teamMembers);
            lore.add(ChatUtils.formatColor(formattedLine));
        }

        ItemStack item = new ItemStack(Material.POPPED_CHORUS_FRUIT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = configManager.getMessageConfig().getString("trophy.winner.name");
            meta.setDisplayName(ChatUtils.formatColor(Optional.ofNullable(displayName).orElse("Winner Trophy")));

            meta.setLore(lore);
            meta.setCustomModelData(101);
            item.setItemMeta(meta);
        }

        return item;
    }

    public ItemStack getPlayerTrophy(Player player, String teamName) {
        List<String> rawLore = configManager.getMessageConfig().getStringList("trophy.player.lore");

        String teamDisplayName = Optional.ofNullable(xTeamsAPI.getTeamDisplayName(teamName)).orElse("Unknown Team");
        String teamMembers = Optional.ofNullable(getTeamMembers(teamName, player)).orElse("No teammates");

        List<String> lore = new ArrayList<>();
        for (String line : rawLore) {
            String formattedLine = line
                    .replace("%player%", Optional.ofNullable(player.getName()).orElse("Unknown"))
                    .replace("%team%", teamDisplayName)
                    .replace("%teammate%", teamMembers);
            lore.add(ChatUtils.formatColor(formattedLine));
        }

        ItemStack item = new ItemStack(Material.SHULKER_SHELL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = configManager.getMessageConfig().getString("trophy.player.name");
            meta.setDisplayName(ChatUtils.formatColor(Optional.ofNullable(displayName).orElse("Participant Trophy")));

            meta.setLore(lore);
            meta.setCustomModelData(101);
            item.setItemMeta(meta);
        }

        return item;
    }

    public void giveTrophy() {
        String teamName = plugin.winnerTeam;
        for (Player target : Bukkit.getOnlinePlayers()) {
            String targetTeamName = xTeamsAPI.getPlayerTeamName(target);

            if (!targetTeamName.equals(teamName)) {
                target.getInventory().addItem(getPlayerTrophy(target, targetTeamName));
                target.sendMessage(chatUtils.getMessage("trophy.givemessage.player", null));
                target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 10f, 2f);
                target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10f, 1.75f);
            } else {
                target.getInventory().addItem(getWinnerTrophy(target, teamName));
                target.sendMessage(chatUtils.getMessage("trophy.givemessage.winner", null));
                target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 10f, 2f);
                target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10f, 1.75f);
            }
        }
    }
    public void giveInvisibility(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 255, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 255, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 255, false, false, false));
    }
}
