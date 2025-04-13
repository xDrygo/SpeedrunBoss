package org.eldrygo.Interface.Managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.eldrygo.Event.Managers.EventDataManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;

import java.util.List;

public class WaitingScreenManager {

    private final SpeedrunBoss plugin;
    private final EventDataManager eventDataManager;
    private final ChatUtils chatUtils;
    private BukkitTask task;
    private long intervalTicks = 20 * 10; // Default: every 10 seconds

    public WaitingScreenManager(SpeedrunBoss plugin, EventDataManager eventDataManager, ChatUtils chatUtils) {
        this.plugin = plugin;
        this.eventDataManager = eventDataManager;
        this.chatUtils = chatUtils;
    }

    public void startWaitingScreen() {
        if (isRunning()) return;

        eventDataManager.setWaitingScreenActive(true);
        plugin.getLogger().info("✅ Waiting Screen is now ON.");

        FileConfiguration config = plugin.getConfig();
        boolean bypassEnabled = config.getBoolean("settings.waiting_screen.bypass.enabled", false);
        List<String> bypassList = config.getStringList("settings.waiting_screen.bypass.list");

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!eventDataManager.isWaitingScreenActive()) {
                stopWaitingScreen();
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (bypassEnabled && bypassList.contains(player.getName())) continue;

                player.sendTitle(
                        chatUtils.getTitle("waiting_screen.title", player),
                        chatUtils.getSubtitle("waiting_screen.subtitle", player),
                        0, 220, 10
                );
            }
        }, 0L, intervalTicks);
    }

    public void stopWaitingScreen() {
        if (!isRunning()) return;

        task.cancel();
        task = null;
        eventDataManager.setWaitingScreenActive(false);
        plugin.getLogger().info("✅ Waiting Screen is now OFF.");
    }

    public boolean isRunning() {
        return task != null && !task.isCancelled();
    }

    public void setIntervalSeconds(long seconds) {
        this.intervalTicks = seconds * 20;

        if (isRunning()) {
            stopWaitingScreen();
            startWaitingScreen();
        }
    }
}
