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
    private int secondsRemaining;
    private final ChatUtils chatUtils;

    public CountdownBossBarManager(SpeedrunBoss plugin, ChatUtils chatUtils) {
        this.plugin = plugin;
        this.chatUtils = chatUtils;
        this.bossBar = Bukkit.createBossBar(chatUtils.getTitle("cinematics.start.bossbar_countdown.string", null), BarColor.PINK, BarStyle.SOLID);
        this.secondsRemaining = 60;
    }

    public void startCountdown(int time) {
        this.secondsRemaining = time;
        updateBossBar(secondsRemaining); // Establece el progreso inicial de la boss bar
        bossBar.setVisible(true);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (secondsRemaining <= 0) {
                    bossBar.setVisible(false);
                    this.cancel();
                    return;
                }

                secondsRemaining--; // Decremento de 1 segundo
                updateBossBar(secondsRemaining); // Actualiza la barra con el nuevo valor de segundos restantes
            }
        }.runTaskTimer(plugin, 0L, 20L); // Ejecuta cada segundo (20 ticks = 1 segundo)
    }

    private void updateBossBar(int timerTime) {
        // Verifica que timerTime no sea cero
        if (timerTime > 0) {
            bossBar.setTitle(chatUtils.getMessage("cinematics.start.bossbar_countdown.string", null).replace("%time%", String.valueOf(secondsRemaining)));
            bossBar.setProgress((double) secondsRemaining / timerTime);
        } else {
            // Si timerTime es cero, establece un valor predeterminado
            bossBar.setProgress(0);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
    }

    public void stopCountdown() {
        bossBar.setVisible(false);
    }
}