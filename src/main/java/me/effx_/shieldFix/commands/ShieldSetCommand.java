package me.effx_.shieldFix.commands;

import me.effx_.shieldFix.ShieldFix;
import me.effx_.shieldFix.utils.DelayService;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ShieldSetCommand implements CommandExecutor, TabCompleter {

    private final ShieldFix plugin;
    private final DelayService service;

    public ShieldSetCommand(ShieldFix plugin) {
        this.plugin = plugin;
        this.service = plugin.getDelayService();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("features.commands.enable", true)) {
            sender.sendMessage(plugin.color("&cCommands are disabled in the config."));
            return true;
        }
        if (!sender.hasPermission("shieldfix.set.others")) {
            sender.sendMessage(plugin.color("&cNo permission."));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(plugin.color("&eUsage: /shieldset <player> <delay>"));
            return true;
        }
        Player target = org.bukkit.Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.color("&cPlayer not online."));
            return true;
        }
        int val = plugin.parseIntSafe(args[1], Integer.MIN_VALUE);
        if (val == Integer.MIN_VALUE) {
            sender.sendMessage(plugin.color("&cInvalid number."));
            return true;
        }
        val = plugin.clamp(val);
        service.set(target.getUniqueId(), val);
        service.saveToConfig();
        sender.sendMessage(plugin.color("&aDelay impostato a &f" + val + " &aper &f" + target.getName()));
        target.sendMessage(plugin.color("&aIl tuo shield delay è stato impostato a &f" + val));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return org.bukkit.Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
