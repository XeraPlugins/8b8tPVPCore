package ca.xera.core.common.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class PluginMessaging {

    private final JavaPlugin plugin;

    public void connect(Player player, String serverName) {
        ByteArrayDataOutput out = newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    @SuppressWarnings("UnstableApiUsage")
    public ByteArrayDataOutput newDataOutput() {
        return ByteStreams.newDataOutput();
    }

    @SuppressWarnings("UnstableApiUsage")
    public ByteArrayDataInput newDataInput(byte[] bytes) {
        return ByteStreams.newDataInput(bytes);
    }
}
