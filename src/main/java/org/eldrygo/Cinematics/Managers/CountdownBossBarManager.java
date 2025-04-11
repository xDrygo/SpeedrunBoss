package org.eldrygo.Cinematics.Managers;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;

public class CountdownBossBarManager {
    private final SpeedrunBoss plugin;
    private final BossBar bossBar;
    public BukkitRunnable bossbarTask;
    private int secondsRemaining;
    private final ChatUtils chatUtils;
    private int totalSeconds;

    public CountdownBossBarManager(SpeedrunBoss plugin, ChatUtils chatUtils) {
        this.plugin = plugin;
        this.chatUtils = chatUtils;
        this.bossBar = Bukkit.createBossBar(chatUtils.getTitle("cinematics.start.bossbar_countdown.string", null), BarColor.PINK, BarStyle.SOLID);
        this.secondsRemaining = 60;
    }

    public void startCountdown(int time) {
        this.secondsRemaining = time;
        this.totalSeconds = time;

        updateBossBar();

        bossBar.setVisible(true);

        bossbarTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (secondsRemaining <= 0) {
                    bossBar.setVisible(false);
                    this.cancel();
                    return;
                }

                secondsRemaining--;
                updateBossBar();
            }
        };

        bossbarTask.runTaskTimer(plugin, 0L, 20L);
    }

    private void updateBossBar() {
        bossBar.setTitle(chatUtils.getMessage("cinematics.start.bossbar_countdown.string", null)
                .replace("%time%", String.valueOf(secondsRemaining)));

        bossBar.setProgress((double) secondsRemaining / totalSeconds);

        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
    }

    public void stopCountdown() {
        bossBar.setVisible(false);
    }
}
