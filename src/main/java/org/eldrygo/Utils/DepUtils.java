package org.eldrygo.Utils;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class DepUtils {
    public String getPrimaryGroup(Player player) {

        RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return "";
        }

        Permission perms = rsp.getProvider();
        return perms.getPrimaryGroup(player);
    }
}
