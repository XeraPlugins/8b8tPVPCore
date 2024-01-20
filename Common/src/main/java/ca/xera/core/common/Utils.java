package ca.xera.core.common;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Utils {

    public static void sendMessage(Player player, String message) {
        player.sendMessage(translate(message));
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(translate(message));
    }

    public static void sendCommandUsage(CommandSender sender, String usage, String command) {
        sendMessage(sender, String.format("&c/%s %s", command, usage));
    }

    public static String translate(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
