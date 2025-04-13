package org.eldrygo.Menu.Inventory.Model;

import org.bukkit.entity.Player;

public class InventoryPlayer {
    private Player player;
    private InventorySection section;

    public InventoryPlayer(Player player) {
        this.player = player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setSection(InventorySection section) {
        this.section = section;
    }

    public Player getPlayer() {
        return player;
    }

    public InventorySection getSection() {
        return section;
    }
}
