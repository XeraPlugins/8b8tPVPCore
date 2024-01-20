package ca.xera.core.velocity.command;

import ca.xera.core.PVPCore;
import ca.xera.core.common.Utils;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Arrays;

public class WhisperCommand implements CommandExecutor, PluginMessageListener {

    private final PVPCore plugin;

    public WhisperCommand(PVPCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "core:chat");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "core:chat" ,this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length > 0) {
                String senderName = sender.getName();
                String targetName = args[0];

                if (senderName.equalsIgnoreCase(targetName)) {
                    Utils.sendMessage(sender, "&cCannot send a message to yourself!");
                    return true;
                }

                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                // check if the target player is on the same server
                Player target = Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().equalsIgnoreCase(targetName)).findAny().orElse(null);
                if (target != null) {
                    Utils.sendMessage(sender, String.format("&bTo %s: %s", targetName, message));
                    Utils.sendMessage(target, String.format("&bFrom %s: %s", senderName, message));
                } else {
                    ByteArrayDataOutput out = PVPCore.messaging.newDataOutput();

                    out.writeUTF(senderName);
                    out.writeUTF(targetName);
                    out.writeUTF(message);

                    plugin.getServer().sendPluginMessage(plugin, "core:chat", out.toByteArray());
                }
            } else Utils.sendCommandUsage(sender, "<player> <message>", label);

        } else Utils.sendMessage(sender, "&cYou must be a player to run this command");
        return true;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (!channel.equals("core:chat")) return;

        ByteArrayDataInput in = PVPCore.messaging.newDataInput(bytes);
        if (in.readUTF().equals("PlayerNotOnline")) {
            String senderName = in.readUTF();
            String targetName = in.readUTF();

            if (player.getName().equals(senderName)) {
                Utils.sendMessage(player, String.format("&c%s is not online!", targetName));
            }
        }
    }
}
