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
    public final BossBar bossBar;
    public final BossBar voidBossBar;
    private final ChatUtils chatUtils;

    public CountdownBossBarManager(ChatUtils chatUtils) {
        this.chatUtils = chatUtils;
        this.voidBossBar = Bukkit.createBossBar("Void Bossbar", BarColor.YELLOW, BarStyle.SOLID);
        this.bossBar = Bukkit.createBossBar(chatUtils.getTitle("cinematics.start.bossbar_countdown.string", null), BarColor.YELLOW, BarStyle.SOLID);
    }

    public void updateBossBar(int totalSeconds, int secondsRemaining) {
        bossBar.setVisible(true);
        bossBar.setTitle(chatUtils.getMessage("cinematics.start.bossbar_countdown.string", null)
                .replace("%time%", String.valueOf(secondsRemaining)));

        bossBar.setProgress((double) secondsRemaining / totalSeconds);

        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
    }
    public void updateVoidBossBar() {
        voidBossBar.setVisible(true);
        voidBossBar.setTitle(" ");

        voidBossBar.setProgress(1);

        for (Player player : Bukkit.getOnlinePlayers()) {
            voidBossBar.addPlayer(player);
        }
    }
}
