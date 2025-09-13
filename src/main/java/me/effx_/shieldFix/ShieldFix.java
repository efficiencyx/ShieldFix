package me.effx_.shieldFix;

import me.effx_.shieldFix.utils.DelayService;
import me.effx_.shieldFix.utils.Metrics;
import me.effx_.shieldFix.events.ShieldListener;
import me.effx_.shieldFix.commands.ShieldDelayCommand;
import me.effx_.shieldFix.commands.ShieldSetCommand;
import me.effx_.shieldFix.features.ShieldPlaceholder;
import me.effx_.shieldFix.utils.Updater;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;
import java.util.OptionalInt;
import java.util.UUID;

public final class ShieldFix extends JavaPlugin {

    private DelayService delayService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // utils
        int pluginId = 22110;
        new Metrics(this, pluginId);

        // delay service (load from config)
        delayService = new DelayService(this);

        // register commands (if enabled)
        if (getConfig().getBoolean("features.commands.enable", true)) {
            getCommand("shieldset").setExecutor(new ShieldSetCommand(this));
            getCommand("shieldset").setTabCompleter(new ShieldSetCommand(this)); // simple tab completer
            getCommand("shielddelay").setExecutor(new ShieldDelayCommand(this));
            getCommand("shielddelay").setTabCompleter(new ShieldDelayCommand(this));
        }

        // PlaceholderAPI soft-depend (simple check)
        if (getConfig().getBoolean("features.papi.enable", true)) {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new ShieldPlaceholder(this).register();
                getLogger().info("PlaceholderAPI expansion registration attempted.");
            } else {
                getLogger().info("PlaceholderAPI enabled in config but plugin not found; skipping registration.");
            }
        }
        getLogger().log(Level.INFO, "§aShield Fix enabled | By effx_");

        if (getConfig().getBoolean("notify-updates")) {
            boolean updateTime = false;
            new Updater(this, 113522).getVersion(version -> {
                if (!this.getDescription().getVersion().equals(version)) {
                    getLogger().info("There is a new update available.");

                }
            });
            // register events (feature toggle inside listener will check config)
            Bukkit.getPluginManager().registerEvents(new ShieldListener(this, updateTime), this);
        }
    }

    @Override
    public void onDisable() {
        // ensure persistence
        delayService.saveToConfig();
    }

    public DelayService getDelayService() {
        return delayService;
    }

    public int getEffectiveDelayFor(org.bukkit.entity.Player p) {
        if (getConfig().getBoolean("features.player_delays.enable", true)) {
            OptionalInt per = delayService.getPerPlayerDelay(p.getUniqueId());
            if (per.isPresent()) return per.getAsInt();
        }

        if (getConfig().getStringList("custom-cooldown-players").contains(p.getName())) {
            return getConfig().getInt("custom-cooldown", 1);
        } else if (p.hasPermission(getConfig().getString("custom-cooldown-perms", "test"))) {
            return getConfig().getInt("custom-cooldown", 1);
        } else {
            return getConfig().getInt("shielddelay", 0);
        }
    }

    public int clamp(int value) {
        int min = getConfig().getInt("limits.min-delay", 0);
        int max = getConfig().getInt("limits.max-delay", 20);
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public int parseIntSafe(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public String color(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }
}
