package ca.xera.core.velocity;

import ca.xera.core.PVPCore;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        String message = String.format("<%s> %s", player.getName(), event.getMessage());
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Message");
        out.writeUTF("ALL");
        out.writeUTF(message);

        player.sendPluginMessage(PVPCore.get(), "BungeeCord", out.toByteArray());
    }
}
