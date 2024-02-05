package ca.xera.core.kit;

import ca.xera.core.common.NBTHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
@Getter
public class Kit {

    private final String name;
    private final NBTTagCompound inventoryTag;

    public void setLoadout(Player player) {
        NBTHelper.setPlayerInventoryFromTag(player, inventoryTag);
    }
}
