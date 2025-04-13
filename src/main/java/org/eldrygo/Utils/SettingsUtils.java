package org.eldrygo.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.eldrygo.BossRace.Listeners.BossKillListener;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.SpeedrunBoss;

import java.util.ArrayList;
import java.util.List;

public class SettingsUtils {
    private final SpeedrunBoss plugin;
    private final PVPManager pvpManager;
    private final GracePeriodManager gracePeriodManager;

    public boolean cinematicBypassEnabled;
    public List<String> cinematicBypassPlayers;
    public double probabilityMultiplier = 0;
    private BossKillListener bossKillListener;

    public SettingsUtils(SpeedrunBoss plugin, PVPManager pvpManager, GracePeriodManager gracePeriodManager, BossKillListener bossKillListener) {
        this.plugin = plugin;
        this.pvpManager = pvpManager;
        this.gracePeriodManager = gracePeriodManager;
        this.bossKillListener = bossKillListener;
    }

    public void loadSettings() {
        plugin.getLogger().info("Loading settings...");
        loadSettingWitherProbabilityMultiplier();
        loadSettingPVPStartDelay();
        loadSettingGracePeriodDuration();
        loadSettingCinematicBypass();
        loadSettingTeamRadiusToBossRegister();
    }

    private void loadSettingWitherProbabilityMultiplier() {
        try {
            probabilityMultiplier = plugin.getConfig().getDouble("settings.wither_skull_multiplier");
            plugin.getLogger().info("✅ Wither Probability Multiplier setting successfully loaded.");
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Failed on loading Wither Probability Multiplier setting: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSettingPVPStartDelay() {
        try {
            long pvpStartDelay = plugin.getConfig().getLong("settings.pvp_start_delay", 20L);
            pvpManager.setPVPStartDelay(pvpStartDelay);
            plugin.getLogger().info("✅ PvP Start Delay setting successfully loaded.");
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Failed on loading PvP Start Delay setting: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSettingGracePeriodDuration() {
        try {
            int gracePeriodDuration = plugin.getConfig().getInt("settings.graceperiod_duration", 1200);
            gracePeriodManager.setGracePeriodDuration(gracePeriodDuration);
            plugin.getLogger().info("✅ Grace Period Duration setting successfully loaded.");
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Failed on loading Grace Period Duration setting: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSettingCinematicBypass() {
        try {
            cinematicBypassEnabled = plugin.getConfig().getBoolean("settings.cinematics.bypass.enabled");
            cinematicBypassPlayers = plugin.getConfig().getStringList("settings.cinematics.bypass.list");
            plugin.getLogger().info("✅ Cinematic Bypass setting successfully loaded.");
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Failed on loading Cinematic Bypass setting: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void loadSettingTeamRadiusToBossRegister() {
        try {
            double team_radius_to_bossregister = plugin.getConfig().getDouble("settings.team_radius_to_bossregister");
            bossKillListener.setCheckRadius(team_radius_to_bossregister);

            plugin.getLogger().info("✅ Cinematic Bypass setting successfully loaded.");
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Failed on loading Cinematic Bypass setting: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
