package me.effx_.shieldFix.utils;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.UUID;
import java.util.OptionalInt;

public class DelayService {

    private final JavaPlugin plugin;
    private final Map<UUID, Integer> playerDelays = new HashMap<>();

    public DelayService(JavaPlugin plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    public OptionalInt getPerPlayerDelay(UUID uuid) {
        if (playerDelays.containsKey(uuid)) return OptionalInt.of(playerDelays.get(uuid));
        return OptionalInt.empty();
    }

    public int getOrDefault(UUID uuid, int def) {
        return playerDelays.getOrDefault(uuid, def);
    }

    public void set(UUID uuid, int value) {
        playerDelays.put(uuid, value);
    }

    public void remove(UUID uuid) {
        playerDelays.remove(uuid);
    }

    public Map<UUID, Integer> all() {
        return Collections.unmodifiableMap(playerDelays);
    }

    public void loadFromConfig() {
        playerDelays.clear();
        try {
            if (plugin.getConfig().isConfigurationSection("player-delays")) {
                for (String key : plugin.getConfig().getConfigurationSection("player-delays").getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(key);
                        int v = plugin.getConfig().getInt("player-delays." + key, Integer.MIN_VALUE);
                        if (v != Integer.MIN_VALUE) playerDelays.put(uuid, v);
                    } catch (IllegalArgumentException ignore) {
                    }
                }
            }
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Unable to load player-delays from config", ex);
        }
    }

    public void saveToConfig() {
        try {
            plugin.getConfig().set("player-delays", null);
            for (Entry<UUID,Integer> e : playerDelays.entrySet()) {
                plugin.getConfig().set("player-delays." + e.getKey().toString(), e.getValue());
            }
            plugin.saveConfig();
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Unable to save player-delays to config", ex);
        }
    }
}
