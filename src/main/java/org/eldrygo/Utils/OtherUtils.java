package org.eldrygo.Utils;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;

public class OtherUtils {
    public static void applyKeepInventoryToAllWorlds() {
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
        }
    }
}
