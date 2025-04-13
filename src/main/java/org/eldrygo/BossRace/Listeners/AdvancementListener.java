package org.eldrygo.BossRace.Listeners;

import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.eldrygo.BossRace.Managers.DimensionBroadcastManager;

public class AdvancementListener implements Listener {
    private final DimensionBroadcastManager dimensionBroadcastManager;

    public AdvancementListener(DimensionBroadcastManager dimensionBroadcastManager) {
        this.dimensionBroadcastManager = dimensionBroadcastManager;
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        Advancement advancement = event.getAdvancement();

        // Ejemplo: detectar si el advancement es "minecraft:story/mine_diamond"
        String key = advancement.getKey().toString();

        if (key.equals("minecraft:story/enter_the_nether") || key.equals("minecraft:story/enter_the_end")) {
            String dimension = getDimension(key);
            dimensionBroadcastManager.onDimensionEnter(dimension, player);
        }
    }

    public String getDimension(String dimensionKey) {
        switch (dimensionKey) {
            case "minecraft:story/enter_the_nether" -> { return "nether"; }
            case "minecraft:story/enter_the_end" -> { return "end"; }
        }
        return "none";
    }
}