package org.eldrygo.BossRace.Listeners;

import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.eldrygo.BossRace.Managers.DimensionBroadcastManager;
import org.eldrygo.Event.Managers.EventManager;

public class AdvancementListener implements Listener {
    private final DimensionBroadcastManager dimensionBroadcastManager;
    private final EventManager eventManager;

    public AdvancementListener(DimensionBroadcastManager dimensionBroadcastManager, EventManager eventManager) {
        this.dimensionBroadcastManager = dimensionBroadcastManager;
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        Advancement advancement = event.getAdvancement();

        String key = advancement.getKey().toString();

        if (eventManager.getState() != EventManager.EventState.RUNNING) return;

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