package ca.xera.core.common;

import lombok.SneakyThrows;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Method;

public class NBTHelper {

    private static Method loadM;
    private static Method createTagM;

    static {
        try {
            createTagM = NBTBase.class.getDeclaredMethod("createTag", byte.class);
            createTagM.setAccessible(true);
            loadM = NBTBase.class.getDeclaredMethod("load", DataInput.class, int.class, NBTReadLimiter.class);
            loadM.setAccessible(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static NBTBase readBaseFromInput(DataInput input) throws Throwable {
        byte typeId = input.readByte();
        if (typeId == 0) return NBTTagEnd.class.newInstance();
        NBTBase base = (NBTBase) createTagM.invoke(NBTBase.class, typeId);
        input.readUTF();
        loadM.invoke(base, input, 0, NBTReadLimiter.a);
        return base;
    }

    @SneakyThrows
    public static NBTTagCompound readNBT(DataInput input) {
        return (NBTTagCompound) readBaseFromInput(input);
    }

    public static void savePlayerInventoryToTaglist(Player player, NBTTagList list) {
        ((CraftInventoryPlayer) player.getInventory()).getInventory().a(list);
    }

    public static void setPlayerInventoryFromTag(Player player, NBTTagCompound compound) {
        ((CraftPlayer) player).getHandle().inventory.b(compound.getList("InvContents", 10));
    }

    @SneakyThrows
    public static void writeNBT(NBTTagCompound compound, DataOutput output) {
        output.writeByte(compound.getTypeId());
        if (compound.getTypeId() == 0) return;
        output.writeUTF("");
        Method writeM = NBTBase.class.getDeclaredMethod("write", DataOutput.class);
        writeM.setAccessible(true);
        writeM.invoke(compound, output);
    }

    @SneakyThrows
    public static NBTTagCompound loadNBTFromFile(File file) {
        FileInputStream fis = new FileInputStream(file);
        DataInputStream in = new DataInputStream(fis);
        NBTTagCompound compound = readNBT(in);
        in.close();
        fis.close();
        return compound;
    }

    @SneakyThrows
    public static void writeAndFlush(NBTTagCompound compound, File file) {
        FileOutputStream fos = new FileOutputStream(file);
        DataOutputStream out = new DataOutputStream(fos);
        NBTHelper.writeNBT(compound, out);
        out.flush();
        out.close();
        fos.close();
    }

    public static void writeLocationToTag(NBTTagCompound compound, Location location) {
        compound.setString("world", location.getWorld().getName());
        compound.setDouble("x", location.getX());
        compound.setDouble("y", location.getY());
        compound.setDouble("z", location.getZ());
        compound.setFloat("yaw", location.getYaw());
        compound.setFloat("pitch", location.getPitch());
    }

    public static Location readLocationFromTag(NBTTagCompound compound) {
        if (!(compound.hasKeyOfType("world", 8) && compound.hasKeyOfType("x", 6) && compound.hasKeyOfType("y", 6) && compound.hasKeyOfType("z", 6) && compound.hasKeyOfType("yaw", 5) && compound.hasKeyOfType("pitch", 5))) {
            return null;
        }
        World world = Bukkit.getWorld(compound.getString("world"));
        if (world == null) {
            return null;
        }
        return new Location(world, compound.getDouble("x"), compound.getDouble("y"), compound.getDouble("z"), compound.getFloat("yaw"), compound.getFloat("pitch"));
    }
}
