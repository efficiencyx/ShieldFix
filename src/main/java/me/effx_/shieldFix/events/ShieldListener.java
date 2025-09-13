package me.effx_.shieldFix.events;

import me.effx_.shieldFix.ShieldFix;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ShieldListener implements Listener {

    private final ShieldFix plugin;
    private final boolean warnUpdate;

    public ShieldListener(ShieldFix plugin, boolean updater) {
        this.plugin = plugin;
        this.warnUpdate = updater;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!e.getPlayer().isOp() || !e.getPlayer().hasPermission("staff")) {
            if (plugin.getConfig().getBoolean("credits")) {
                Player p = e.getPlayer();
                // Why did i do this, please just kill me
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lShield Fix 1.6 By @zNecron"));
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This server is running ShieldFix 1.6"));
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lDowload here"));
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bhttps://www.spigotmc.org/resources/must-have-shield-delay-remover-shield-fix.113522"));
            }
        } else if (e.getPlayer().getName().equals("effx_") && !plugin.getConfig().getBoolean("credits")) {
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lRunning Shield Fix 1.6"));
        }else {
            if (warnUpdate) e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lRunning Shield Fix 1.6"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlocking(PlayerInteractEvent e) {
        if (!e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.SHIELD)) return;
        if (!plugin.getConfig().getBoolean("features.shield_event.enable", true)) return;

        Player p = e.getPlayer();
        int delayToApply = plugin.getEffectiveDelayFor(p);

        int min = plugin.getConfig().getInt("limits.min-delay", 0);
        int max = plugin.getConfig().getInt("limits.max-delay", 20);
        delayToApply = Math.max(min, Math.min(max, delayToApply));

        if (!e.isCancelled()) {
            p.setShieldBlockingDelay(delayToApply);
        } else {
            p.setShieldBlockingDelay(plugin.getConfig().getInt("shielddelay", 0));
        }
    }
}
