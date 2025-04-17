package org.eldrygo.Time.Managers;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;

public class TimeBarManager {

    private final TimeManager timeManager;
    private final BossBar decorativeBar;
    private final BossBar timeBar;
    private BukkitRunnable task;
    private final SpeedrunBoss plugin;
    private final String titleTemplate;

    public TimeBarManager(TimeManager timeManager, ConfigManager configManager, SpeedrunBoss plugin) {
        this.plugin = plugin;
        this.timeManager = timeManager;
        this.titleTemplate = configManager.getMessageConfig().getString("time_bar.bossbar", "&f%time%");
        this.timeBar = Bukkit.createBossBar(" ", BarColor.YELLOW, BarStyle.SOLID);
        this.timeBar.setVisible(false);
        this.decorativeBar = Bukkit.createBossBar(" ", BarColor.YELLOW, BarStyle.SOLID);
        this.decorativeBar.setVisible(false);
    }

    // Inicia el task que actualiza el bossbar
    public void startUpdating() {
        if (task != null) task.cancel();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                String title;
                if (timeManager.isRunning()) {
                    long time = timeManager.getElapsedTime();
                    String formatted = formatTime(time);
                    title = titleTemplate.replace("%time%", formatted);
                } else {
                    title = titleTemplate.replace("%time%", "paused");
                }

                timeBar.setTitle(ChatUtils.formatColor(title));

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!decorativeBar.getPlayers().contains(player)) {
                        decorativeBar.addPlayer(player);
                    }
                    if (!timeBar.getPlayers().contains(player)) {
                        timeBar.addPlayer(player);
                    }
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L); // Actualiza cada segundo (20 ticks)
    }

    // Formatea segundos como mm:ss o hh:mm:ss
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        timeBar.removeAll();
        decorativeBar.removeAll();
    }

    public void showAllBars() {
        decorativeBar.setVisible(true);
        timeBar.setVisible(true);
    }

    public void hideAllBars() {
        timeBar.setVisible(false);
        decorativeBar.setVisible(false);
    }
}
