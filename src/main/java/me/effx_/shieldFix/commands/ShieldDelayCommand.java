package me.effx_.shieldFix.commands;

import me.effx_.shieldFix.ShieldFix;
import me.effx_.shieldFix.utils.DelayService;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ShieldDelayCommand implements CommandExecutor, TabCompleter {

    private final ShieldFix plugin;
    private final DelayService service;

    public ShieldDelayCommand(ShieldFix plugin) {
        this.plugin = plugin;
        this.service = plugin.getDelayService();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("features.commands.enable", true)) {
            sender.sendMessage(plugin.color("&cCommands are disabled in the config."));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(plugin.color("&eUsage: /shielddelay <get|set|inc|dec|reload> ..."));
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        try {
            switch (sub) {
                case "get": {
                    if (args.length == 1) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(plugin.color("&cUsa: /shielddelay get <player>"));
                            return true;
                        }
                        Player p = (Player) sender;
                        int eff = plugin.getEffectiveDelayFor(p);
                        sender.sendMessage(plugin.color("&aIl tuo delay attuale è &f" + eff));
                        return true;
                    } else {
                        if (!sender.hasPermission("shieldfix.get.others")) {
                            sender.sendMessage(plugin.color("&cNo permission."));
                            return true;
                        }
                        Player target = org.bukkit.Bukkit.getPlayerExact(args[1]);
                        if (target == null) { sender.sendMessage(plugin.color("&cPlayer not online.")); return true; }
                        int eff = plugin.getEffectiveDelayFor(target);
                        sender.sendMessage(plugin.color("&aDelay di &f" + target.getName() + " &a= &f" + eff));
                        return true;
                    }
                }
                case "set": {
                    if (args.length < 2) {
                        sender.sendMessage(plugin.color("&eUsage: /shielddelay set <value> [player]"));
                        return true;
                    }
                    int val = plugin.parseIntSafe(args[1], Integer.MIN_VALUE);
                    if (val == Integer.MIN_VALUE) { sender.sendMessage(plugin.color("&cValore non valido.")); return true; }
                    if (args.length == 2) {
                        if (!(sender instanceof Player)) { sender.sendMessage(plugin.color("&cSolo i giocatori possono impostare il proprio delay.")); return true; }
                        if (!sender.hasPermission("shieldfix.set.self")) { sender.sendMessage(plugin.color("&cNo permission.")); return true; }
                        Player p = (Player) sender;
                        val = plugin.clamp(val);
                        service.set(p.getUniqueId(), val);
                        service.saveToConfig();
                        p.sendMessage(plugin.color("&aDelay impostato a &f" + val));
                        return true;
                    } else {
                        if (!sender.hasPermission("shieldfix.set.others")) { sender.sendMessage(plugin.color("&cNo permission.")); return true; }
                        Player target = org.bukkit.Bukkit.getPlayerExact(args[2]);
                        if (target == null) { sender.sendMessage(plugin.color("&cPlayer non online.")); return true; }
                        val = plugin.clamp(val);
                        service.set(target.getUniqueId(), val);
                        service.saveToConfig();
                        sender.sendMessage(plugin.color("&aDelay impostato a &f" + val + " &aper &f" + target.getName()));
                        target.sendMessage(plugin.color("&aIl tuo delay è stato impostato a &f" + val));
                        return true;
                    }
                }
                case "inc":
                case "+": {
                    if (!(sender instanceof Player)) { sender.sendMessage(plugin.color("&cOnly players.")); return true; }
                    Player p = (Player) sender;
                    if (!sender.hasPermission("shieldfix.modify.self")) { sender.sendMessage(plugin.color("&cNo permission.")); return true; }
                    int amt = 1;
                    if (args.length >= 2) {
                        amt = plugin.parseIntSafe(args[1], Integer.MIN_VALUE);
                        if (amt == Integer.MIN_VALUE) { sender.sendMessage(plugin.color("&cValore non valido.")); return true; }
                    }
                    int current = service.getOrDefault(p.getUniqueId(), plugin.getConfig().getInt("shielddelay", 0));
                    int result = plugin.clamp(current + amt);
                    service.set(p.getUniqueId(), result); service.saveToConfig();
                    p.sendMessage(plugin.color("&aDelay aumentato a &f" + result));
                    return true;
                }
                case "dec":
                case "-": {
                    if (!(sender instanceof Player)) { sender.sendMessage(plugin.color("&cOnly players.")); return true; }
                    Player p = (Player) sender;
                    if (!sender.hasPermission("shieldfix.modify.self")) { sender.sendMessage(plugin.color("&cNo permission.")); return true; }
                    int amt = 1;
                    if (args.length >= 2) {
                        amt = plugin.parseIntSafe(args[1], Integer.MIN_VALUE);
                        if (amt == Integer.MIN_VALUE) { sender.sendMessage(plugin.color("&cValore non valido.")); return true; }
                    }
                    int current = service.getOrDefault(p.getUniqueId(), plugin.getConfig().getInt("shielddelay", 0));
                    int result = plugin.clamp(current - amt);
                    service.set(p.getUniqueId(), result); service.saveToConfig();
                    p.sendMessage(plugin.color("&aDelay diminuito a &f" + result));
                    return true;
                }
                case "reload": {
                    if (!sender.hasPermission("shieldfix.reload")) { sender.sendMessage(plugin.color("&cNo permission.")); return true; }
                    service.loadFromConfig();
                    sender.sendMessage(plugin.color("&aConfig ricaricata."));
                    return true;
                }
                default:
                    sender.sendMessage(plugin.color("&eComando sconosciuto. Usa get/set/inc/dec/reload"));
                    return true;
            }
        } catch (Exception ex) {
            sender.sendMessage(plugin.color("&cErrore interno: " + ex.getMessage()));
            plugin.getLogger().severe("Command error: " + ex.getMessage());
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        if (name.equals("shieldset") || name.equals("shielddelay")) {
            if (args.length == 1) {
                List<String> subs = Arrays.asList("get", "set", "inc", "dec", "reload");
                return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
            } else if (args.length == 2) {
                return null; // allow player names by bukkit
            } else if (args.length == 3) {
                List<String> pl = org.bukkit.Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                return pl.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[2].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
