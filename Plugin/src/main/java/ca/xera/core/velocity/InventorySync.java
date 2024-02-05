package ca.xera.core.velocity;

import ca.xera.core.common.NBTHelper;
import com.google.common.io.ByteStreams;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class InventorySync implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (channel.equals("xera:invreply")) {
            NBTTagCompound replied = NBTHelper.readNBT(ByteStreams.newDataInput(bytes));
            NBTHelper.setPlayerInventoryFromTag(player, replied);
        }
    }
}
