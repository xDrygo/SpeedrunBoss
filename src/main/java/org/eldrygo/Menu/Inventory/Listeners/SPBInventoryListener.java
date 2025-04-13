package org.eldrygo.Menu.Inventory.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.eldrygo.Menu.Inventory.Managers.SPBInventoryManager;
import org.eldrygo.Menu.Inventory.Model.InventoryPlayer;
import org.eldrygo.SpeedrunBoss;

public class SPBInventoryListener implements Listener {
    private SpeedrunBoss plugin;
    private final SPBInventoryManager spbInventoryManager;

    public SPBInventoryListener(SpeedrunBoss plugin, SPBInventoryManager spbInventoryManager) {
        this.plugin = plugin;
        this.spbInventoryManager = spbInventoryManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        InventoryPlayer inventoryPlayer = spbInventoryManager.getInventoryPlayer(player);
        if (inventoryPlayer != null) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getClickedInventory().equals(player.getOpenInventory().getTopInventory())) {
                spbInventoryManager.inventoryClick(inventoryPlayer, event.getSlot(), event);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        spbInventoryManager.removePlayer(player);
    }
}
