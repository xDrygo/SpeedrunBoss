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
    private BukkitRunnable countdownTask;
    private int secondsRemaining;
    private final ChatUtils chatUtils;
    private int timerTime;

    public CountdownBossBarManager(SpeedrunBoss plugin, ChatUtils chatUtils) {
        this.plugin = plugin;
        this.chatUtils = chatUtils;
        this.bossBar = Bukkit.createBossBar(chatUtils.getTitle("cinematics.start.bossbar_countdown.string", null), BarColor.PINK, BarStyle.SOLID);
        this.secondsRemaining = 60;
    }

    public void startCountdown(int time) {
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        secondsRemaining = time;
        timerTime = time;

        updateBossBar(timerTime);
        bossBar.setVisible(true);

        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (secondsRemaining <= 0) {
                    bossBar.setVisible(false);
                    this.cancel();
                    return;
                }

                secondsRemaining--;
                updateBossBar(timerTime);
            }
        };

        countdownTask.runTaskTimer(plugin, 0L, 20L); // Ejecutar cada segundo
    }

    private void updateBossBar(int timerTime) {
        bossBar.setTitle(chatUtils.getMessage("cinematics.start.bossbar_countdown.string", null).replace("%time%", String.valueOf(secondsRemaining)));
        bossBar.setProgress((double) secondsRemaining / timerTime);

        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
    }

    public void stopCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        bossBar.setVisible(false);
    }
}
