package me.effx_.shieldFix.features;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.effx_.shieldFix.ShieldFix;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShieldPlaceholder extends PlaceholderExpansion {

    private final ShieldFix plugin;

    public ShieldPlaceholder(ShieldFix plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "shieldfix";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().isEmpty() ? "effx_" : plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // keep loaded
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        // %shieldfix_delay% -> player's effective delay
        // %shieldfix_delay_<player>% -> other player's effective delay (if online)
        if (identifier == null) return "";
        if (identifier.equalsIgnoreCase("delay")) {
            if (p == null) return "";
            return String.valueOf(plugin.getEffectiveDelayFor(p));
        }
        if (identifier.toLowerCase().startsWith("delay_")) {
            String name = identifier.substring("delay_".length());
            if (name.isEmpty()) return "";
            Player other = Bukkit.getPlayerExact(name);
            if (other == null) return "";
            return String.valueOf(plugin.getEffectiveDelayFor(other));
        }
        return "";
    }
}
